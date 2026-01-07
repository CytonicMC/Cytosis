package net.cytonic.protocol;

public interface Message<T, R> {

    ProtocolObject<T, R> getProtocolObject();

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
}
