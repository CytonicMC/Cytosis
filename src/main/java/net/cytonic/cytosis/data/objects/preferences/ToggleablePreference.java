package net.cytonic.cytosis.data.objects.preferences;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

public class ToggleablePreference extends Preference<Boolean> {

    /**
     * Creates a new {@link Preference}, with an optionally null value. The type must be specified manually
     *
     * @param key   the namespace
     * @param value the default value, nullable
     */
    public ToggleablePreference(Key key, @Nullable Boolean value) {
        super(Boolean.class, key, value);
    }

    @Override
    public Preference<Boolean> withValue(@Nullable Boolean value) {
        return new ToggleablePreference(getKey(), value);
    }

    @Override
    public Preference<Boolean> fromStorage(StoredPreference preference) {
        return new ToggleablePreference(getKey(), Boolean.parseBoolean(preference.getValue()));
    }

    @Override
    public StoredPreference toStorage() {
        return new StoredPreference(getKey(), getValue().toString());
    }

    @Override
    public Preference<Boolean> clone() {
        return new ToggleablePreference(getKey(), getValue());
    }
}
