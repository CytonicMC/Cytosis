package net.cytonic.cytosis.snooper;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.ebean.DB;
import io.ebean.Transaction;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.snooper.query.QQueriedSnoop;
import net.cytonic.cytosis.utils.EbeanUtils;
import net.cytonic.cytosis.utils.Msg;

public class SnoopPersistenceManager {

    private static final int MAX_CACHE_SIZE = 250; // Adjust based on memory usage

    @SneakyThrows
    public CompletableFuture<Void> persistSnoop(SnooperChannel channel, Component component) {
        return CompletableFuture.supplyAsync(() -> {
            String message = Msg.stripTags(Msg.toText(component));
            try (Transaction transaction = DB.beginTransaction()) {
                DB.save(new QueriedSnoop(channel.id().asString(), channel.recipients(), message));
                transaction.commit();
            }
            return null;
        });
    }

    public CompletableFuture<List<QueriedSnoop>> query(String channel, byte target, @Nullable Instant start,
        @Nullable Instant end, boolean ascending, @Nullable String search) {
        return CompletableFuture.supplyAsync(() -> {
            QQueriedSnoop queriedSnoop = new QQueriedSnoop()
                .target.eq(target)
                .target.ne((byte) 0)
                .setMaxRows(1000)
                .channel.eq(channel)
                .content.icontains(search);
            if (start != null && end != null) {
                queriedSnoop.created.between(start, end);
            } else {
                queriedSnoop.created.ne(Instant.EPOCH);
            }
            EbeanUtils.orderByInstant(queriedSnoop.created, ascending);
            return queriedSnoop.findList();
        });
    }
}
