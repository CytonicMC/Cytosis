package net.cytonic.cytosis.data.objects.preferences;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.logging.Logger;

/**
 * A class that acts a registry holding the registered preferences
 */
@SuppressWarnings({"unused"})
@NoArgsConstructor
public class PreferenceRegistry {

    private final Map<Key, Preference<?>> preferences = new ConcurrentHashMap<>();


    /**
     * Writes a preference to the registry
     *
     * @param value the preference
     */
    public <T> void write(@NotNull Preference<T> value) {
        if (preferences.containsKey(value.getKey())) {
            throw new IllegalArgumentException(
                "There is already a preference registered under the namespace " + value.getKey());
        }
        if (value.getValue() != null && !value.getType().isAssignableFrom(value.getValue().getClass())) {
            throw new IllegalArgumentException(
                "Default value of preference " + value.getKey() + " does not conform to its type!");
        }
        preferences.put(value.getKey(), value);
    }

    /**
     * If this registry contains a preference by the given namespace
     *
     * @param preference the namespace
     * @return if the registry contains the preference
     */
    public boolean contains(Preference<?> preference) {
        if (preference == null) {
            return false;
        }
        return contains(preference.getKey());
    }

    /**
     * If this registry contains a preference by the given namespace
     *
     * @param key the namespace
     * @return if the registry contains the preference
     */
    public boolean contains(Key key) {
        if (key == null) {
            return false;
        }
        return preferences.containsKey(key);
    }


    /**
     * The set of namespaces contained in this registry
     *
     * @return the set of namespaces
     */
    public Set<Key> keys() {
        return new HashSet<>(preferences.keySet());
    }

    @Nullable
    public Class<?> getTypeFromNamespace(Key key) {
        Set<Preference<?>> entries = preferences.values().stream()
            .filter(e -> e.getKey().equals(key))
            .collect(Collectors.toSet());
        if (entries.isEmpty()) {
            return null;
        }
        if (entries.size() > 1) {
            throw new IllegalStateException("Multiple preferences registered under the name id!");
        }
        Preference<?> val = Iterables.getFirst(entries, null);
        if (val == null) return null;
        return val.getType();
    }

    public boolean isJson(Key key) {
        return get(key) instanceof JsonPreference<?>;
    }

    /**
     * Gets a preference entry in this registry
     *
     * @param namespace the namespace
     * @return the entry tied to the namespace
     * @throws IllegalArgumentException if the entry does not exist
     */
    @Nullable
    public Preference<?> get(Key namespace) {
        if (!contains(namespace)) {
            Logger.warn("There is no preference registered under the namespace " + namespace);
        }
        return preferences.get(namespace);
    }

    /**
     * Gets a preference entry in this registry
     *
     * @param key the key
     * @param <T> the type of the requested preference, and return entry type
     * @return the entry tied to the key
     * @throws IllegalArgumentException if the entry does not match the requested type
     * @throws IllegalArgumentException if the entry does not exist
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Preference<T> get(Key key, Class<T> type) {
        if (!contains(key)) {
            Logger.warn("There is no preference registered under the key " + key);
        }
        Preference<T> val = (Preference<T>) preferences.get(key);
        if (val == null) return null;
        if (!val.getType().isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                key + " doesn't not match the type! (Expected: " + type.getSimpleName() + ", Got: " + val.getType()
                    .getSimpleName() + ")");
        }
        return val;
    }
}
