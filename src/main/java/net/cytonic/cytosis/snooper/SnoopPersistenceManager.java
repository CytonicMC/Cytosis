package net.cytonic.cytosis.snooper;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.HibernateHelper;
import net.cytonic.cytosis.utils.Msg;

public class SnoopPersistenceManager {

    private static final int MAX_CACHE_SIZE = 250; // Adjust based on memory usage

    @SneakyThrows
    public CompletableFuture<Void> persistSnoop(SnooperChannel channel, Component component) {
        return CompletableFuture.supplyAsync(() -> {
            String message = Msg.stripTags(Msg.toText(component));
            EntityTransaction transaction = null;
            try (EntityManager manager = Cytosis.get(HibernateHelper.class).createSession()) {
                transaction = manager.getTransaction();
                transaction.begin();
                manager.persist(new QueriedSnoop(channel.id().asString(), channel.recipients(), message));
                transaction.commit();
            } catch (Exception e) {
                Logger.warn("Failed to persist snooper channel!");
                e.printStackTrace(System.err);
                if (transaction != null) {
                    transaction.rollback();
                }
            }
            return null;
        });
    }

    public CompletableFuture<List<QueriedSnoop>> query(String channel, byte target, @Nullable Instant start,
        @Nullable Instant end, boolean ascending, @Nullable String search) {
        return CompletableFuture.supplyAsync(() -> {

            try (EntityManager manager = Cytosis.get(HibernateHelper.class).createSession()) {
                QQueriedSnoop qSnoop = QQueriedSnoop.queriedSnoop;
                return new JPAQuery<QueriedSnoop>(manager)
                    .from(qSnoop)
                    .where(qSnoop.target.eq(target)
                        .and(qSnoop.target.ne((byte) 0))
                        .and(qSnoop.channel.eq(channel))
                        .and(start != null && end != null ?
                            qSnoop.created.between(start, end)
                            : qSnoop.created.ne(Instant.EPOCH))
                        .and(qSnoop.content.containsIgnoreCase(search))
                    )
                    .orderBy(ascending ? qSnoop.created.asc() : qSnoop.created.desc())
                    .limit(1000)
                    .fetch();
            }
        });
    }
}
