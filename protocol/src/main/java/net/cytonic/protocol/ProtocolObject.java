package net.cytonic.protocol;

import java.util.function.Consumer;

import net.cytonic.protocol.utils.NatsAPI;

public abstract class ProtocolObject<T, R> implements Serializable<T>, ReturnSerializable<R>, Subject {

    public abstract Serializer<T> getSerializer();

    public void request(T message, Consumer<byte[]> onResponse) {
        request(getSubject(), message, onResponse);
    }

    public void request(String subject, T message, Consumer<byte[]> onResponse) {
        NatsAPI.INSTANCE.request(subject, getSerializer().serialize(message), onResponse);
    }

    public abstract Serializer<R> getReturnSerializer();

    public abstract String getSubject();
    //todo figure out if we want simple class names or subjects
//    public String channel() {
//        return getClass().getSimpleName();
//    }
}
