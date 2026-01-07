package net.cytonic.protocol;

import java.util.function.Consumer;

import net.cytonic.protocol.utils.NatsAPI;

public abstract class NoResponse<T> extends ProtocolObject<T, Void> {

    @Override
    public void request(T message, Consumer<byte[]> onResponse) {
        throw new UnsupportedOperationException("Don't implement NoResponse if you need a response!");
    }

    public void publish(T message) {
        publish(getSubject(), message);
    }

    public void publish(String subject, T message) {
        NatsAPI.INSTANCE.publish(subject, getSerializer().serialize(message));
    }

    @Override
    public Serializer<Void> getReturnSerializer() {
        return null;
    }
}
