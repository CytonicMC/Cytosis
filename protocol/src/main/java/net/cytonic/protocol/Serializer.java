package net.cytonic.protocol;

public interface Serializer<T> {

    String serialize(T value);

    T deserialize(String json);
}
