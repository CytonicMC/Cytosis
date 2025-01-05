package net.cytonic.cytosis.data.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.JsonPreference;
import net.cytonic.cytosis.data.objects.preferences.NamespacedPreference;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.objects.preferences.PreferenceRegistry;
import net.cytonic.cytosis.logging.Logger;
import net.minestom.server.utils.NamespaceID;

import java.io.IOException;
import java.util.UUID;

/**
 * A type adapter for {@link Preference}, allow Gson to serialize and deserialize it easily.
 * The stored syntax is as follows: {@code {"namespace":"cytosis:some_namespace","value:"some value. Not required to be a string, but many are."}}
 *
 * @param <T> The type of the preference
 */
@SuppressWarnings("preview")
@NoArgsConstructor
public class PreferenceAdapter<T> extends TypeAdapter<Preference<?>> implements TypeAdapterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(JsonWriter out, Preference<?> value) throws IOException {
        if (!(value instanceof NamespacedPreference<?> pref)) { // json pref is an instance of namespaced
            throw new JsonParseException("Unsupported preference type: " + value.getClass().getName());
        }

        out.beginObject();
        out.name("id");
        out.value(pref.namespace().asString()); // "cytosis:some_namespce"

        out.name("value");
        switch (value.value()) {
            case String str -> out.value(str);
            case Number num -> out.value(num);
            case Boolean bool -> out.value(bool);
            case UUID uuid -> out.value(uuid.toString());
            case Enum<?> constant -> out.value(constant.name());
            case null -> out.nullValue();
            default -> {
                if (pref instanceof JsonPreference<?> json) {
                    out.value(json.serialize());
                } else
                    throw new UnsupportedOperationException(STR."Unsupported type: \{value.value().getClass().getName()}");
            }
        }

        out.endObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Preference<T> read(JsonReader in) throws IOException {
        in.beginObject();

        Object value = null;
        String rawID = null;

        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("value")) {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                } else if (in.peek() == JsonToken.STRING) {
                    value = in.nextString();
                } else if (in.peek() == JsonToken.NUMBER) {
                    value = in.nextDouble();  // Use nextDouble() for general number handling
                } else if (in.peek() == JsonToken.BOOLEAN) {
                    value = in.nextBoolean();
                }
            } else if (name.equals("id")) {
                rawID = in.nextString();
            } else in.skipValue();
        }

        in.endObject();

        if (rawID == null) throw new JsonParseException("Preference deserialization failed: No id found");

        NamespaceID id = NamespaceID.from(rawID);

        Class<T> type = (Class<T>) Cytosis.getPreferenceManager().getPreferenceRegistry().getTypeFromNamespace(id);
        PreferenceRegistry.Entry<T> preference = Cytosis.getPreferenceManager().getPreferenceRegistry().get(new TypedNamespace<>(id, type));

        Logger.debug(preference.preference().getClass().getSimpleName() + " " + id.asString());
        if (preference.preference() instanceof JsonPreference<T> json) {
            return new JsonPreference<>(id, type, json.deserialize(value.toString())); // should already be a string....
        }

        // Convert value to the correct type if it's not null
        if (type == UUID.class && value != null) {
            assert value instanceof String;
            value = UUID.fromString((String) value);
        } else if (type.isEnum() && value != null) {
            assert value instanceof String;
            value = Enum.valueOf((Class<Enum>) type, (String) value);
        }
        TypedNamespace<T> tn = new TypedNamespace<>(id, type);
        return new NamespacedPreference<>(tn, type.cast(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (!Preference.class.isAssignableFrom(type.getRawType())) {
            return null; // This factory does not handle this type
        }
        return (TypeAdapter<R>) gson.getDelegateAdapter(this, TypeToken.get(Preference.class));
    }
}
