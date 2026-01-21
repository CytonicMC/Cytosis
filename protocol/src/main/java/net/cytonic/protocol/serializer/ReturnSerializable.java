package net.cytonic.protocol.serializer;

public interface ReturnSerializable<T> {

    Serializer<T> getReturnSerializer();

    default String serializeReturnToString(T message) {
        return getReturnSerializer().serialize(message);
    }

    default T deserializeReturnFromString(String string) {
        return getReturnSerializer().deserialize(string);
    }
}
