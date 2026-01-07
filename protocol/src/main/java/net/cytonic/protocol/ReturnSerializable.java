package net.cytonic.protocol;

public interface ReturnSerializable<T> {

    Serializer<T> getReturnSerializer();

    default String translateReturnToString(T message) {
        return getReturnSerializer().serialize(message);
    }

    default T translateReturnFromString(String string) {
        return getReturnSerializer().deserialize(string);
    }
}
