package net.cytonic.cytosis.data.objects;

import com.google.common.collect.Iterables;
import net.cytonic.objects.NamespacedPreference;
import net.cytonic.objects.Preference;
import net.cytonic.objects.TypedNamespace;
import net.minestom.server.utils.NamespaceID;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings({"preview", "unused"})
public class PreferenceRegistry {
    private final Map<TypedNamespace<?>, Entry<?>> preferences = new ConcurrentHashMap<>();

    public <T> void write(TypedNamespace<T> namespace, Preference<T> value) {
        if (!namespace.type().equals(value.value().getClass()))
            throw new IllegalArgumentException(STR."The preference value must be of type \{namespace.type().getSimpleName()}");
        preferences.put(namespace, new Entry<>(namespace, value));
    }

    public <T> void write(TypedNamespace<T> namespace, NamespacedPreference<T> value) {
        if (!namespace.namespaceID().equals(value.namespaceID().namespaceID()))
            throw new IllegalArgumentException(STR."The preference namespace must be \{namespace.namespaceID()}");
        if (!namespace.type().equals(value.value().getClass()))
            throw new IllegalArgumentException(STR."The preference value must be of type \{namespace.type().getSimpleName()}");
        preferences.put(namespace, new Entry<>(namespace, new Preference<>(value.value())));
    }

    @SuppressWarnings("unchecked")
    public <T> Entry<T> get(TypedNamespace<T> namespace) {
        Entry<?> entry = preferences.get(namespace);
        if (entry != null && entry.namespaceID().type().equals(namespace.type())) {
            return (Entry<T>) entry;
        }
        throw new ClassCastException("Entry type does not match the requested type.");
    }

    public boolean contains(TypedNamespace<?> namespace) {
        return preferences.containsKey(namespace);
    }

    public Set<NamespaceID> namespaces() {
        return preferences.keySet().stream().map(TypedNamespace::namespaceID).collect(Collectors.toSet());
    }

    @Nullable
    public Preference<?> getUNSAFE(NamespaceID namespaceID) {
        Set<Entry<?>> ids = preferences.values().stream().filter(e -> e.namespaceID().namespaceID().asString()
                .equals(namespaceID.asString())).collect(Collectors.toSet());
        return Iterables.getFirst(ids, null).value();
    }

    public record Entry<T>(TypedNamespace<T> namespaceID, Preference<T> value) {
        // records are cool
    }
}
