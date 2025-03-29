package net.cytonic.cytosis.data.objects.preferences;

import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;


public class NamespacedPreference<T> extends Preference<T> {

    private final TypedNamespace<T> namespaceID;

    /**
     * Creates a new {@link NamespacedPreference}, with an optionally null value. The type must be specified manually
     *
     * @param namespaceID the namespace
     * @param value       the default value, nullable
     */
    public NamespacedPreference(TypedNamespace<T> namespaceID, @Nullable T value) {
        super(namespaceID.type(), namespaceID.type().cast(value));
        this.namespaceID = namespaceID;
    }

    public NamespacedPreference(Key namespaceID, Class<T> type, @Nullable T value) {
        super(type, type.cast(value));
        this.namespaceID = new TypedNamespace<>(namespaceID, type);
    }

    public Key namespace() {
        return namespaceID.namespaceID();
    }

    public TypedNamespace<T> typedNamespace() {
        return namespaceID;
    }

    @Override
    public NamespacedPreference<T> clone() {
        return new NamespacedPreference<>(namespaceID.namespaceID(), type(), Utils.clone(value()));
    }
}
