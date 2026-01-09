package net.cytonic.protocol;

public interface NotifyListener<T> extends Subject, Serializable<T> {

    ProtocolObject<T, ?> getProtocolObject();

    @Override
    default Serializer<T> getSerializer() {
        return getProtocolObject().getSerializer();
    }

    void onMessage(T message);

    @Override
    default String getSubject() {
        return getProtocolObject().getSubject();
    }
}
