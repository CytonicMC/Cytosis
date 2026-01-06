package net.cytonic.cytosis.data.packet.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public abstract class ReusableRequestPacket<T extends RequestPacket<T, R>, R extends Packet<R>> extends
    RequestPacket<T, R> {

    @Override
    @Deprecated(forRemoval = true) // trickery to get IntelliJ to yell at you
    public String getSubject() {
        throw new UnsupportedOperationException("getSubject() cannot be called on a reusable packet.");
    }

    @Override
    @Deprecated(forRemoval = true) // trickery to get IntelliJ to yell at you
    public void request(BiConsumer<R, Throwable> consumer) {
        throw new UnsupportedOperationException("Requesting without a subject is not permitted on a reusable packet.");
    }

    @Override
    @Deprecated(forRemoval = true) // trickery to get IntelliJ to yell at you
    public CompletableFuture<R> request() {
        throw new UnsupportedOperationException("Requesting without a subject is not permitted on a reusable packet.");
    }
}
