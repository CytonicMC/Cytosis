package net.cytonic.cytosis.data.serializers;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.NatsManager;

public abstract class RequestPacket<Req extends RequestPacket<Req, Resp>, Resp extends Packet<Resp>>
    extends Packet<Req> {

    public CompletableFuture<Resp> send() {
        return send(getDefaultTimeout());
    }

    public CompletableFuture<Resp> send(Duration timeout) {
        CompletableFuture<Resp> future = new CompletableFuture<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        Cytosis.get(NatsManager.class).request(getSubject(), getData(), (message, throwable) -> {
            if (!completed.compareAndSet(false, true)) return;

            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            if (message == null) {
                future.completeExceptionally(new IllegalStateException("Null message received"));
                return;
            }

            try {
                Serializer<Resp> serializer = Packet.getSerializer(getResponseType());
                Resp response = serializer.deserialize(getSubject(), new String(message.getData()));
                future.complete(response);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (completed.compareAndSet(false, true)) {
                future.completeExceptionally(new TimeoutException("Request timed out"));
            }
        }).repeat(TaskSchedule.duration(timeout));
        return future;
    }

    protected abstract Class<Resp> getResponseType();

    protected Duration getDefaultTimeout() {
        return Duration.ofSeconds(5);
    }
}