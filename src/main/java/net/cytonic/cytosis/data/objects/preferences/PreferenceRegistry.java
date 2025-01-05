package net.cytonic.cytosis.data.objects.preferences;

import com.google.common.collect.Iterables;
import lombok.NoArgsConstructor;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A class that acts a registry holding the registered preferences
 */
@SuppressWarnings({"preview", "unused"})
@NoArgsConstructor
public class PreferenceRegistry {
    private final Map<TypedNamespace<?>, Entry<?>> preferences = new ConcurrentHashMap<>();

    /**
     * Writes a preference to the registry
     *
     * @param namespace the namespace
     * @param value     the preference
     * @param <T>       the type of the preference
     */
    public <T> void write(@NotNull TypedNamespace<T> namespace, @NotNull Preference<T> value) {
        if (preferences.containsKey(namespace))
            throw new IllegalArgumentException(STR."There is already a preference registered under the namespace \{namespace.namespaceID().asString()}");
        if (value.value() != null && !namespace.type().equals(value.value().getClass()))
            throw new IllegalArgumentException(STR."The preference value must be of type \{namespace.type().getSimpleName()}");
        preferences.put(namespace, new Entry<>(namespace, value));
    }

    /**
     * Writes a preference to the registry
     *
     * @param namespace the namespace
     * @param value     a namespaced preference
     * @param <T>       the type of the preference
     */
    public <T> void write(TypedNamespace<T> namespace, @NotNull NamespacedPreference<T> value) {
        if (value.value() == null) {
            write(namespace, (Preference<T>) value);
            return;
        }
        if (!namespace.namespaceID().equals(value.namespace()))
            throw new IllegalArgumentException(STR."The preference namespace must be \{namespace.namespaceID()}");
        if (!namespace.type().equals(value.value().getClass()))
            throw new IllegalArgumentException(STR."The preference value must be of type \{namespace.type().getSimpleName()}");
        write(namespace, (Preference<T>) value);
    }

    /**
     * Gets a preference entry in this registry
     *
     * @param namespace the namespace
     * @param <T>       the type of the requested preference, and return entry type
     * @return the entry tied to the namespace
     * @throws IllegalArgumentException if the entry does not match the requested type
     * @throws IllegalArgumentException if the entry does not exist
     */
    @SuppressWarnings("unchecked")
    public <T> Entry<T> get(TypedNamespace<T> namespace) {
        if (!contains(namespace))
            throw new IllegalArgumentException(STR."There is no preference registered under the namespace \{namespace.namespaceID().asString()}");

        Entry<?> entry = preferences.get(namespace);

        if (entry.namespaceID().type().equals(namespace.type())) {
            return (Entry<T>) entry;
        }
        throw new IllegalArgumentException("Entry type does not match the requested type.");
    }

    /**
     * If this registry contains a preference by the given namespace
     *
     * @param namespace the namespace
     * @return if the registry contains the preference
     */
    public boolean contains(TypedNamespace<?> namespace) {
        if (namespace == null) return false;
        return preferences.containsKey(namespace);
    }

    /**
     * If this registry contains a preference by the given namespace
     *
     * @param preference the namespace
     * @return if the registry contains the preference
     */
    public boolean contains(NamespacedPreference<?> preference) {
        if (preference == null) return false;
        return contains(preference.typedNamespace());
    }

    /**
     * An unsafe version of {@link #contains(TypedNamespace)}, but it doesn't require a typed namespace.
     * <p> Don't use this if you can avoid it.
     *
     * @param namespaceID the namespace
     * @return if the registry contains the preference
     */
    @ApiStatus.Internal
    public boolean contains_UNSAFE(NamespaceID namespaceID) {
        if (namespaceID == null) return false;
        return preferences.keySet().stream().map(TypedNamespace::namespaceID).collect(Collectors.toSet()).contains(namespaceID);
    }

    /**
     * The set of namespaces contained in this registry
     *
     * @return the set of namespaces
     */
    public Set<NamespaceID> namespaces() {
        return preferences.keySet().stream().map(TypedNamespace::namespaceID).collect(Collectors.toSet());
    }

    /**
     * Gets the typed namespaces contained in this registry
     *
     * @return the set of typed namespaces
     */
    public Set<TypedNamespace<?>> typedNamespaces() {
        return preferences.keySet();
    }

    /**
     * An unsafe version of {@link #get(TypedNamespace)}, but it doesn't require a typed namespace. Its unsafe because it cannot
     * guarantee type safety.
     *
     * @param namespaceID the namespace
     * @return the preference
     */
    @ApiStatus.Internal
    @Nullable
    public Preference<?> get_UNSAFE(NamespaceID namespaceID) {
        Set<Entry<?>> ids = preferences.values().stream().filter(e -> e.namespaceID().namespaceID().equals(namespaceID)).collect(Collectors.toSet());
        return Iterables.getFirst(ids, null).preference();
    }

    public Class<?> getTypeFromNamespace(NamespaceID namespaceID) {
        Set<Entry<?>> entries = preferences.values().stream().filter(e -> e.namespaceID().namespaceID().equals(namespaceID)).collect(Collectors.toSet());
        if (entries.isEmpty()) return null;
        if (entries.size() > 1) throw new IllegalStateException("Multiple preferences registered under the name id!");
        return Iterables.getFirst(entries, null).preference().type();
    }

    /**
     * A record representing a registry entry
     *
     * @param namespaceID the namespace
     * @param preference  the preference
     * @param <T>         the type of the preference
     */
    public record Entry<T>(TypedNamespace<T> namespaceID, Preference<T> preference) {
        // records are cool
    }
}
