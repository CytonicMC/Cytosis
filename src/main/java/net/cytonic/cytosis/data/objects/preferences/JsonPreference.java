package net.cytonic.cytosis.data.objects.preferences;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.utils.Utils;

@Getter
@Setter
public class JsonPreference<T> extends Preference<T> {

    private final Codec<T> codec;

    public JsonPreference(Key key, Class<T> type, Codec<T> codec, @Nullable T value) {
        super(type, key, value);
        this.codec = codec;
    }

    @Override
    public Preference<T> withValue(@Nullable T value) {
        return new JsonPreference<>(getKey(), getType(), getCodec(), value);
    }

    @Override
    public Preference<T> fromStorage(StoredPreference preference) {
        return new JsonPreference<>(getKey(), getType(), getCodec(), deserialize(preference.getValue()));
    }

    @Override
    public StoredPreference toStorage() {
        return new StoredPreference(getKey(), serialize());
    }

    @Override
    public JsonPreference<T> clone() {
        return new JsonPreference<>(getKey(), getType(), getCodec(), Utils.clone(getValue()));
    }

    public String serialize() {
        return Utils.toJson(getValue(), codec);
    }

    public T deserialize(String data) {
        return Utils.parseJson(data, codec);
    }
}
