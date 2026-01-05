package net.cytonic.cytosis.data.packet.utils;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;

import net.cytonic.cytosis.Cytosis;

@AllArgsConstructor
public class DefaultGsonSerializer<T> implements Serializer<T> {

    private final Class<T> type;
    private Gson gson = Cytosis.GSON;

    public DefaultGsonSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public String serialize(T value) {
        return gson.toJson(value);
    }

    @Override
    public T deserialize(String json) {
        return gson.fromJson(json, type);
    }
}
