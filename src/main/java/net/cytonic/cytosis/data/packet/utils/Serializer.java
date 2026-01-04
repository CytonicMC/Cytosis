package net.cytonic.cytosis.data.packet.utils;

public interface Serializer<T> {

    String serialize(String subject, T value);

    T deserialize(String subject, String json);
}