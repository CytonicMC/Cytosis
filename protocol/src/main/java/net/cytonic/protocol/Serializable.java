package net.cytonic.protocol;

public interface Serializable<T> {

    Serializer<T> getSerializer();

    default String serializeToString(T message) {
        return getSerializer().serialize(message);
    }

    default T deserializeFromString(String string) {
        return getSerializer().deserialize(string);
    }
}
