package net.cytonic.protocol;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.serializer.ReturnSerializable;
import net.cytonic.protocol.serializer.Serializable;
import net.cytonic.protocol.utils.NatsAPI;

public abstract class ProtocolObject<T, R> implements Serializable<T>, ReturnSerializable<R>, Subject {

    public void request(T message, BiConsumer<R, Throwable> onResponse) {
        request(getSubject(), message, onResponse);
    }

    public void request(String subject, T message, BiConsumer<R, @Nullable Throwable> onResponse) {
        NatsAPI.INSTANCE.request(subject, serializeToString(message),
            (bytes, throwable) -> onResponse.accept(deserializeReturnFromString(new String(bytes)), throwable));
    }
}
