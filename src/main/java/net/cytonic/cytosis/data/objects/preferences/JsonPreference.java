package net.cytonic.cytosis.data.objects.preferences;

import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.utils.Utils;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class JsonPreference<T> extends NamespacedPreference<T> {

    private JsonPreferenceSerializer<T> serializer = Cytosis.GSON::toJson;
    private JsonPreferenceDeserializer<T> deserializer = data -> Cytosis.GSON.fromJson(data, type());

    /**
     * Creates a new {@link JsonPreference}, with an optionally null value. The type must be specified manually
     *
     * @param namespaceID the namespace
     * @param type        the type of the preference
     * @param value       the default value, nullable
     */
    public JsonPreference(NamespaceID namespaceID, Class<T> type, @Nullable T value) {
        super(namespaceID, type, value);
    }

    public JsonPreference(TypedNamespace<T> namespaceID, @NotNull T value) {
        super(namespaceID, value);
    }

    public JsonPreference(NamespaceID namespaceID, Class<T> type, @Nullable T value, JsonPreferenceSerializer<T> serializer, JsonPreferenceDeserializer<T> deserializer) {
        super(namespaceID, type, value);
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public JsonPreference(TypedNamespace<T> namespaceID, @NotNull T value, JsonPreferenceSerializer<T> serializer, JsonPreferenceDeserializer<T> deserializer) {
        super(namespaceID, value);
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public String serialize() {
        return serializer.serialize(value());
    }

    public T deserialize(String data) {
        return deserializer.deserialize(data);
    }

    public T deserializeAndSet(String data) {
        value(deserializer.deserialize(data));
        return value();
    }

    @Override
    public JsonPreference<T> clone() {
        return new JsonPreference<>(namespace(), type(), Utils.clone(value()), serializer, deserializer);
    }
}
