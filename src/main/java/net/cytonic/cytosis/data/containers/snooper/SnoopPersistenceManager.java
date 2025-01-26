package net.cytonic.cytosis.data.containers.snooper;

import lombok.SneakyThrows;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SnoopPersistenceManager {
    private static final int MAX_CACHE_SIZE = 250; // Adjust based on memory usage

    // jooq stuff
    private final DSLContext db;
    private final Table<?> table = DSL.table("cytonic_snoops");
    private final Field<Integer> id = DSL.field("id", Integer.class);
    private final Field<Byte> target = DSL.field("target", Byte.class);
    private final Field<String> content = DSL.field("content", String.class);
    private final Field<String> channel = DSL.field("channel", String.class);
    private final Field<Timestamp> created = DSL.field("created", Timestamp.class);

    // LRU Cache for queries
    private final Map<QueryKey, List<QueriedSnoop>> queryCache = new ConcurrentHashMap<>();
    private final LinkedHashMap<QueryKey, Boolean> evictionQueue = new LinkedHashMap<>(100, 0.75f, true);

    public SnoopPersistenceManager(MysqlDatabase db) {
        this.db = DSL.using(db.getConnection(), SQLDialect.MYSQL);

        // make sure the tables exist
        createTables();
    }

    @SneakyThrows
    public void createTables() {
        try {

            if (db.createTableIfNotExists("cytonic_snoops")
                    .column("id", SQLDataType.INTEGER.identity(true))
                    .column("channel", SQLDataType.VARCHAR(255).nullable(false))
                    .column("target", SQLDataType.TINYINT.nullable(false))
                    .column("content", SQLDataType.CLOB.nullable(false))
                    .column("created", SQLDataType.TIMESTAMP.nullable(false).defaultValue(DSL.currentTimestamp()))
                    .constraint(DSL.primaryKey("id"))
                    .execute() != 0) {
                db.execute("ALTER TABLE cytonic_snoops ADD FULLTEXT (content)");
            }
        } catch (Exception e) {
            Logger.error("Failed to set up snooper database!", e);
        }
    }

    @SneakyThrows
    public CompletableFuture<Void> persistSnoop(SnooperChannel channel, Component component) {
        return CompletableFuture.supplyAsync(() -> {
            String message = MiniMessage.miniMessage().stripTags(PlainTextComponentSerializer.plainText().serialize(component));
            db.insertInto(table, target, content, this.channel)
                    .values(channel.recipients(), message, channel.id().asString())
                    .execute();

            invalidateCache(); // Invalidate cache on insert, as the db changed
            return null;
        });
    }

    public CompletableFuture<List<QueriedSnoop>> query(String id, byte permission, Range.Int pagination, @Nullable Instant start, @Nullable Instant end, boolean ascending, @Nullable String search) {
        return CompletableFuture.supplyAsync(() -> {
            SelectConditionStep<Record> query = db.select().from(table)
                    .where(DSL.bitAnd(target, permission).ne((byte) 0))
                    .and(channel.eq(id))
                    .and(start != null && end != null ? created.between(Timestamp.from(start), Timestamp.from(end)) : created.ne(Timestamp.from(Instant.EPOCH)));

            if (search != null && !search.isEmpty()) {
                query = query.and("MATCH(content) AGAINST(? IN BOOLEAN MODE)", search);
            }

            Result<Record> result = query.orderBy(ascending ? created.desc() : created.asc())
                    .limit(pagination.max() - pagination.min())
                    .offset(pagination.min())
                    .fetch();

            return processSnoops(result);
        });
    }

    private void prefetchPages(String id, byte permission, Range.Int pagination, @Nullable Instant start, @Nullable Instant end, boolean ascending, @Nullable String search) {
        for (int i = 1; i <= 2; i++) { // Prefetch next 2 pages
            int perPage = (pagination.max() - pagination.min());
            int newMin = pagination.min() + (i * perPage);
            int newMax = newMin + perPage;
            Range.Int newPagination = new Range.Int(newMin, newMax);
            QueryKey prefetchKey = new QueryKey(id, permission, newPagination, start, end, ascending, search);

            if (!queryCache.containsKey(prefetchKey)) {
                query(id, permission, newPagination, start, end, ascending, search).whenComplete((queriedSnoops, throwable) -> {
                    if (throwable != null) {
                        Logger.error("Failed to prefetch page!", throwable);
                        return;
                    }
                    cacheResult(prefetchKey, queriedSnoops);
                });
            }
        }
    }

    private List<QueriedSnoop> processSnoops(Result<org.jooq.Record> result) {
        return result.stream().map(record -> {
            return new QueriedSnoop(
                    record.get(id),
                    record.get(target),
                    record.get(content),
                    record.get(channel),
                    record.get(created)
            );
        }).toList();
    }

    // caching stuff
    private synchronized void cacheResult(QueryKey key, List<QueriedSnoop> result) {
        if (queryCache.size() >= MAX_CACHE_SIZE) {
            Iterator<QueryKey> it = evictionQueue.keySet().iterator();
            if (it.hasNext()) {
                QueryKey oldestKey = it.next();
                queryCache.remove(oldestKey);
                it.remove();
            }
        }
        queryCache.put(key, result);
        evictionQueue.put(key, true);
    }

    private synchronized List<QueriedSnoop> getCachedResult(QueryKey key) {
        if (evictionQueue.containsKey(key)) {
            evictionQueue.remove(key);
            evictionQueue.put(key, true);
            return queryCache.get(key);
        }
        return null;
    }

    public synchronized void invalidateCache() {
        evictionQueue.clear();
        queryCache.clear();
    }

    private record QueryKey(String id, byte permission, Range.Int pagination, @Nullable Instant start,
                            @Nullable Instant end, boolean ascending, @Nullable String search) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QueryKey that = (QueryKey) o;
            return that.id.equals(this.id) &&
                    that.permission == this.permission &&
                    that.pagination.max() == this.pagination.max() &&
                    that.pagination.min() == this.pagination.min() &&
                    Objects.equals(this.start, that.start) &&
                    Objects.equals(this.end, that.end) &&
                    this.ascending == that.ascending &&
                    Objects.equals(search, that.search);
        }
    }
}
