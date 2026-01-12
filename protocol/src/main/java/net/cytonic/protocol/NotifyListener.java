package net.cytonic.protocol;

public interface NotifyListener<T> extends Subject, Serializable<T> {

    default ProtocolObject<T, ?> getProtocolObject() {
        return ProtocolHelper.getProtocolObject(getClass(), 0);
    }

    @Override
    default Serializer<T> getSerializer() {
        return getProtocolObject().getSerializer();
    }

    void onMessage(T message, NotifyData notifyData);

    @Override
    default String getSubject() {
        return getProtocolObject().getSubject();
    }
}
