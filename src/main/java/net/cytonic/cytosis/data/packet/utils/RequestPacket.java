package net.cytonic.cytosis.data.packet.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public abstract class RequestPacket<Req extends RequestPacket<Req, Resp>, Resp extends Packet<Resp>>
    extends Packet<Req> {

    public void request(String subject, BiConsumer<Resp, Throwable> consumer) {
        request(subject).whenComplete(consumer);
    }

    public CompletableFuture<Resp> request(String subject) {
        CompletableFuture<Resp> future = new CompletableFuture<>();
        NATS_MANAGER.request(subject, getData(), (message, throwable) -> {

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
                Resp response = serializer.deserialize(new String(message.getData()));
                future.complete(response);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public void request(BiConsumer<Resp, Throwable> consumer) {
        request(getSubject(), consumer);
    }

    public CompletableFuture<Resp> request() {
        return request(getSubject());
    }

    protected abstract Class<Resp> getResponseType();
}