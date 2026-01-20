package net.cytonic.protocol.serializer;

public interface Serializer<T> {

    String serialize(T value);

    T deserialize(String json);
}
