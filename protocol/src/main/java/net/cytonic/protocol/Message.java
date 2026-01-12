package net.cytonic.protocol;

import java.util.function.BiConsumer;

public interface Message<T, R> {

    default ProtocolObject<T, R> getProtocolObject() {
        return ProtocolHelper.getProtocolObject(getClass(), 0);
    }

    default void publish() {
        publish(getProtocolObject().getSubject());
    }

    @SuppressWarnings("unchecked")
    default void publish(String subject) {
        ProtocolObject<T, R> protocolObject = getProtocolObject();
        if (protocolObject instanceof NoResponse) {
            NoResponse<T> noResponse = (NoResponse<T>) protocolObject;
            noResponse.publish(subject, (T) this);
            return;
        }
        throw new UnsupportedOperationException("Don't implement NoResponse if you need a response!");
    }

    default void request(BiConsumer<R, Throwable> onResponse) {
        request(getProtocolObject().getSubject(), onResponse);
    }

    @SuppressWarnings("unchecked")
    default void request(String subject, BiConsumer<R, Throwable> onResponse) {
        getProtocolObject().request(subject, (T) this, onResponse);
    }
}
