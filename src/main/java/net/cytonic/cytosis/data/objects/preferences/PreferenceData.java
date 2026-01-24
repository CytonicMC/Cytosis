package net.cytonic.cytosis.data.objects.preferences;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.PreferenceManager;

/**
 * A class holding a user's preference data.
 */
public class PreferenceData {

    public static PreferenceManager pm = Cytosis.get(PreferenceManager.class);

    public static final Codec<PreferenceData> CODEC = StoredPreference.CODEC.list()
        .transform(PreferenceData::new, PreferenceData::toStorage);

    private final Map<Key, Preference<?>> preferences;

    /**
     * Creates a new PreferenceData object with the specified preferences
     *
     * @param list the preferences
     */
    public PreferenceData(List<StoredPreference> list) {
        this.preferences = list.stream()
            .map(p -> {
                Preference<?> known = pm.getPreferenceRegistry().get(p.getKey());
                if (known == null)
                    return p; // this instance doesn't use this preference, just keep it as a stored version
                return known.fromStorage(p);
            }).collect(Collectors.toMap(Preference::getKey, Function.identity()));
    }

    /**
     * Deserializes the string into a {@link PreferenceData} object.
     *
     * @param data The serialized json data
     * @return a new {@link PreferenceData} object with the specified data
     */
    public static PreferenceData deserialize(String data) {
        return CODEC.decode(Transcoder.JSON, JsonParser.parseString(data))
            .orElseThrow("Failed to parse user preferences!");
    }

    public List<StoredPreference> toStorage() {
        return preferences.values().stream().map(Preference::toStorage).toList();
    }

    @Nullable
    public Preference<?> getPreference(Key key) {
        return preferences.get(key);
    }

    /**
     * Gets a preference of the specified type. If the player does not have a set preference, it will return the default
     * value.
     *
     * @param key The namespace of the preference
     * @param <T> The type of the preference
     * @return The preference. Null if it does not exist
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Key key) {
        if (preferences.containsKey(key)) {
            return (T) preferences.get(key).getValue();
        } else {
            return (T) PreferenceManager.PREFERENCE_REGISTRY.get(key).getValue();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getOr(Key key, T defaultValue) {
        try {
            if (preferences.containsKey(key)) {
                return (T) preferences.get(key).getValue();
            } else {
                return (T) PreferenceManager.PREFERENCE_REGISTRY.get(key).getValue();
            }
        } catch (NullPointerException _) {
            return defaultValue;
        }
    }

    /**
     * Gets a preference of the specified type. If the player does not have a set preference, it will return the default
     * value. Possibly null.
     *
     * @param namespace The namespaced ID to pull the namespace from
     * @param <T>       The type of the preference
     * @return The preference. Null if it does not exist
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Preference<T> namespace) {
        if (preferences.containsKey(namespace.getKey())) {
            return (T) preferences.get(namespace.getKey()).getValue();
        } else {
            return namespace.getValue();
        }
    }

    public <T> void set(Preference<T> p) {
        set(p.getKey(), p.getType(), p.getValue());
    }

    /**
     * Sets the value of the specified preference
     *
     * @param key   The namespace of the preference
     * @param value The new value
     * @param <T>   The type of the preference
     */
    public <T> void set(Key key, Class<T> type, T value) {
        if (!PreferenceManager.PREFERENCE_REGISTRY.contains(key))
            throw new IllegalArgumentException("Unknown preference: " + key);
        Preference<T> pref = PreferenceManager.PREFERENCE_REGISTRY.get(key, type);
        preferences.put(key, pref.withValue(value));
    }

    /**
     * Serializes the preference data into a json string
     *
     * @return the JSON data of this object
     */
    public String serialize() {
        return Cytosis.GSON.toJson(CODEC.encode(Transcoder.JSON, this)
            .orElseThrow("Failed to serialize user preferences!"));
    }

    public Set<Key> keys() {
        return preferences.keySet();
    }

    @Override
    public String toString() {
        return "PreferenceData [preferences=" + preferences + "]";
    }
}
