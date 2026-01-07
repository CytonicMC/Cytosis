package net.cytonic.protocol;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import lombok.AllArgsConstructor;
import net.kyori.adventure.key.Key;

import net.cytonic.protocol.adapters.InstantAdapter;
import net.cytonic.protocol.adapters.JsonComponentAdapter;
import net.cytonic.protocol.adapters.KeyAdapter;
import net.cytonic.protocol.data.objects.JsonComponent;

@AllArgsConstructor
public class GsonSerializer<T> implements Serializer<T> {

    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Key.class, new KeyAdapter())
        .registerTypeAdapter(Instant.class, new InstantAdapter())
        .registerTypeAdapter(JsonComponent.class, new JsonComponentAdapter())
        .registerTypeAdapterFactory(new KeyAdapter())
        .enableComplexMapKeySerialization()
        .setStrictness(Strictness.LENIENT)
        .serializeNulls()
        .create();

    private final Class<T> clazz;
    private Gson gson = GSON;

    public GsonSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String serialize(T value) {
        return gson.toJson(value, clazz);
    }

    @Override
    public T deserialize(String json) {
        return gson.fromJson(json, clazz);
    }
}
