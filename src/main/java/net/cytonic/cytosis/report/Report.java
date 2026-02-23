package net.cytonic.cytosis.report;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.ebean.DB;
import lombok.AllArgsConstructor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.utils.Utils;

@AllArgsConstructor
public record Report<T extends ReportType<T>>(UUID uuid, ReportContext<T> context, T type, UUID reporter, UUID player,
                                              Instant submittedAt, boolean resolved) {

    @SuppressWarnings("unchecked")
    static <T extends ReportType<T>> Report<T> from(ReportEntity entity) {
        ReportType<?> rawType = Cytosis.get(ReportManager.class).getReportType(entity.getType());
        if (rawType == null) {
            throw new UnsupportedOperationException(
                "The report type \"" + entity.getType() + "\" is not supported on this server!");
        }
        T type = (T) rawType;
        ReportContext<T> ctx = Utils.parseJson(entity.getContext(), type.getContextCodec());

        return new Report<>(entity.getId(), ctx, type, entity.getReporter(), entity.getPlayer(),
            entity.getSubmittedAt(), entity.isResolved());
    }

    public static <T extends ReportType<T>> Report<T> create(T type, ReportContext<T> ctx, UUID reporter, UUID player) {
        return new Report<>(UUID.randomUUID(), ctx, type, reporter, player, Instant.now(), false);
    }

    public CompletableFuture<Void> markResolved() {
        return CompletableFuture.supplyAsync(() -> {
            DB.update(ReportEntity.class)
                .set("resolved", reporter())
                .where().idEq(uuid)
                .update();
            return null;
        });
    }
}
