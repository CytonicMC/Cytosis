package net.cytonic.cytosis.data.containers.snooper;

import lombok.SneakyThrows;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
            try {
                db.insertInto(table, target, content, this.channel)
                        .values(channel.recipients(), message, channel.id().asString())
                        .execute();
            } catch (Exception e) {
                Logger.warn("Failed to persist snooper channel!");
            }

            return null;
        });
    }

    public CompletableFuture<List<QueriedSnoop>> query(String id, byte permission, @Nullable Instant start, @Nullable Instant end, boolean ascending, @Nullable String search) {
        return CompletableFuture.supplyAsync(() -> {
            SelectConditionStep<Record> query = db.select().from(table)
                    .where(DSL.bitAnd(target, permission).ne((byte) 0))
                    .and(channel.eq(id))
                    .and(start != null && end != null ? created.between(Timestamp.from(start), Timestamp.from(end)) : created.ne(Timestamp.from(Instant.EPOCH)));

            if (search != null && !search.isEmpty()) {
                query = query.and("MATCH(content) AGAINST(? IN BOOLEAN MODE)", search);
            }

            Result<Record> result = query.orderBy(ascending ? created.desc() : created.asc())
                    .limit(1000)
                    .fetch();

            return processSnoops(result);
        });
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


    private record QueryKey(String id, byte permission, @Nullable Instant start,
                            @Nullable Instant end, boolean ascending, @Nullable String search) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QueryKey that = (QueryKey) o;
            return that.id.equals(this.id) &&
                    that.permission == this.permission &&
                    Objects.equals(this.start, that.start) &&
                    Objects.equals(this.end, that.end) &&
                    this.ascending == that.ascending &&
                    Objects.equals(search, that.search);
        }
    }
}
