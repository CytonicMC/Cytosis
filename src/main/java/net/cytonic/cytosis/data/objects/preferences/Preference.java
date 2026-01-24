package net.cytonic.cytosis.data.objects.preferences;

import java.util.UUID;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.utils.Utils;

@Getter
public class Preference<T> {

    private final Key key;

    private final Class<T> type;

    private final T value;


    /**
     * Creates a new {@link Preference}, with an optionally null value. The type must be specified manually
     *
     * @param key   the namespace
     * @param value the default value, nullable
     */
    public Preference(Class<T> clazz, Key key, @Nullable T value) {
        this.type = clazz;
        this.value = value;
        this.key = key;
    }

    public Preference<T> withValue(@Nullable T value) {
        return new Preference<>(type, key, value);
    }

    public Preference<T> fromStorage(StoredPreference preference) {
        if (!preference.getKey().equals(key)) throw new IllegalArgumentException(
            "Stored preference key does not match runtime preference key! " + preference.getKey() + " != " + key);
        return new Preference<>(type, key, parseValue(preference.getValue()));
    }


    public StoredPreference toStorage() {
        if (value == null) {
            return new StoredPreference(key, "null");
        }
        return new StoredPreference(key, value.toString());
    }

    private T parseValue(String raw) {
        if (raw.equals("null")) return null;
        if (type.isAssignableFrom(String.class)) return type.cast(raw);
        if (type.isAssignableFrom(UUID.class)) return type.cast(UUID.fromString(raw));
        if (type.isAssignableFrom(Double.class)) return type.cast(Double.parseDouble(raw));
        if (type.isAssignableFrom(Float.class)) return type.cast(Float.parseFloat(raw));
        if (type.isAssignableFrom(Integer.class)) return type.cast(Integer.parseInt(raw));
        if (type.isAssignableFrom(Short.class)) return type.cast(Short.parseShort(raw));
        if (type.isAssignableFrom(Long.class)) return type.cast(Long.parseLong(raw));
        if (type.isAssignableFrom(Byte.class)) return type.cast(Byte.parseByte(raw));
        if (type.isAssignableFrom(Boolean.class)) return type.cast(Boolean.parseBoolean(raw));
        if (type.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<? extends Enum> enumType = (Class<? extends Enum>) type;
            @SuppressWarnings("unchecked")
            T enumValue = (T) Enum.valueOf(enumType, raw);
            return enumValue;
        }
        throw new RuntimeException("Failed to parse preference value: " + raw);
    }

    @Override
    public Preference<T> clone() {
        return new Preference<>(type, key, Utils.clone(value));
    }
}
