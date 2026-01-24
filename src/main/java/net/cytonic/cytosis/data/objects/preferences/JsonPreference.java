package net.cytonic.cytosis.data.objects.preferences;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.utils.Utils;

@Getter
@Setter
public class JsonPreference<T> extends Preference<T> {

    //todo: use codecs :)
    private JsonPreferenceSerializer<T> serializer = Cytosis.GSON::toJson;
    private JsonPreferenceDeserializer<T> deserializer = data -> Cytosis.GSON.fromJson(data, getType());

    /**
     * Creates a new {@link JsonPreference}, with an optionally null value. The type must be specified manually
     *
     * @param key   the namespace
     * @param type  the type of the preference
     * @param value the default value, nullable
     */
    public JsonPreference(Key key, Class<T> type, @Nullable T value) {
        super(type, key, value);
    }

    public JsonPreference(Key key, Class<T> type, @Nullable T value, JsonPreferenceSerializer<T> serializer,
        JsonPreferenceDeserializer<T> deserializer) {
        super(type, key, value);
        this.serializer = serializer;
        this.deserializer = deserializer;
    }


    public String serialize() {
        return serializer.serialize(getValue());
    }

    public T deserialize(String data) {
        return deserializer.deserialize(data);
    }

    @Override
    public JsonPreference<T> clone() {
        return new JsonPreference<>(getKey(), getType(), Utils.clone(getValue()), serializer, deserializer);
    }
}
