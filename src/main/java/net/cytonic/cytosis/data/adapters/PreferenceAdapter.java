package net.cytonic.cytosis.data.adapters;

import java.io.IOException;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.FallbackPreference;
import net.cytonic.cytosis.data.objects.preferences.JsonPreference;
import net.cytonic.cytosis.data.objects.preferences.NamespacedPreference;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.objects.preferences.PreferenceRegistry;
import net.cytonic.cytosis.managers.PreferenceManager;

/**
 * A type adapter for {@link Preference}, allow Gson to serialize and deserialize it easily. The stored syntax is as
 * follows:
 * {@code {"namespace":"cytosis:some_namespace","value:"some value. Not required to be a string, but many are."}}
 *
 * @param <T> The type of the preference
 */
@NoArgsConstructor
public class PreferenceAdapter<T> extends TypeAdapter<Preference<?>> implements TypeAdapterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(JsonWriter out, Preference<?> value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("Null preferences are not supported");
        }
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
            case null -> {
                // fallbacks values are null, as  they keep them specially
                if (value instanceof FallbackPreference<?> fallback) {
                    out.value(fallback.getRawValue());
                } else {
                    out.nullValue();
                }
            }
            default -> {
                if (pref instanceof JsonPreference<?> json) {
                    out.value(json.serialize());
                } else {
                    throw new UnsupportedOperationException("Unsupported type: " + value.value().getClass()
                        .getName() + " for preference type " + pref.getClass()
                        .getSimpleName());
                }
            }
        }

        out.endObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Preference<T> read(JsonReader in) throws IOException {
        Object value = null;
        String rawID = null;

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("value")) {
                value = readValue(in);
            } else if (name.equals("id")) {
                rawID = in.nextString();
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        if (rawID == null) {
            throw new JsonParseException("Preference deserialization failed: No group found");
        }

        Key id = Key.key(rawID);
        Class<T> type = (Class<T>) Cytosis.CONTEXT.getComponent(PreferenceManager.class)
            .getPreferenceRegistry()
            .getTypeFromNamespace(id);

        return buildPreference(id, type, value);
    }

    private Object readValue(JsonReader in) throws IOException {
        return switch (in.peek()) {
            case NULL -> {
                in.nextNull();
                yield null;
            }
            case STRING -> in.nextString();
            case NUMBER -> in.nextDouble();
            case BOOLEAN -> in.nextBoolean();
            default -> {
                in.skipValue();
                yield null;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private Preference<T> buildPreference(Key id, Class<T> type, Object value) {
        try {
            PreferenceRegistry.Entry<T> preference = Cytosis.CONTEXT.getComponent(PreferenceManager.class)
                .getPreferenceRegistry()
                .get(new TypedNamespace<>(id, type));

            if (preference.preference() instanceof JsonPreference<T> json) {
                return new JsonPreference<>(id, type,
                    value == null ? null : json.deserialize(value.toString()));
            }
        } catch (IllegalArgumentException e) {
            return new FallbackPreference<>(new TypedNamespace<>(id, (Class<T>) String.class),
                String.valueOf(value));
        }

        if (value != null) {
            if (type == UUID.class) {
                value = UUID.fromString((String) value);
            } else if (type.isEnum()) {
                value = Enum.valueOf((Class<Enum>) type, (String) value);
            } else if (type == Boolean.class && value instanceof String s) {
                value = Boolean.valueOf(s);
            }
        }

        return new NamespacedPreference<>(new TypedNamespace<>(id, type), type.cast(value));
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