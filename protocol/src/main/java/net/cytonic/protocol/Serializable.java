package net.cytonic.protocol;

public interface Serializable<T> {

    Serializer<T> getSerializer();

    default String translateToString(T message) {
        return getSerializer().serialize(message);
    }

    default T translateFromString(String string) {
        return getSerializer().deserialize(string);
    }
}
