package net.cytonic.cytosis.data.packet.utils;

public interface Serializer<T> {

    String serialize(T value);

    T deserialize(String input);
}