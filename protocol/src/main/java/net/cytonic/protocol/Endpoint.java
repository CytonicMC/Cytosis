package net.cytonic.protocol;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.utils.ReflectionUtils;

public interface Endpoint<T, R> extends Subject {

    default ProtocolObject<T, R> getProtocolObject() {
        return ProtocolHelper.getProtocolObject(ReflectionUtils.getTypeName(getClass(), 0));
    }

    @Nullable
    CompletableFuture<R> onMessage(T message, NotifyData extraData);
}