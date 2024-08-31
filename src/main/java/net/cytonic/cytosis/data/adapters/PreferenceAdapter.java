package net.cytonic.cytosis.data.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.cytonic.objects.Preference;

import java.io.IOException;
import java.util.UUID;

/**
 * A type adapter for {@link Preference}, allow Gson to serialize and deserialize it easily.
 * @param <T> The type of the preference
 */
@SuppressWarnings("preview")
public class PreferenceAdapter<T> extends TypeAdapter<Preference<?>> implements TypeAdapterFactory {
    /**
     * A default constructor
     */
    public PreferenceAdapter() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(JsonWriter out, Preference<?> value) throws IOException {

        out.beginObject();

        // Serialize NamespaceID
        out.name("value");
        Object val = value.value();

        switch (val) {
            case String str -> out.value(str);
            case Number num -> out.value(num);
            case Boolean bool -> out.value(bool);
            case UUID uuid -> out.value(uuid.toString());
            case Enum<?> constant -> out.value(constant.name());
            case null -> out.nullValue();
            default -> throw new UnsupportedOperationException(STR."Unsupported type: \{val.getClass().getName()}");
        }

        // Serialize Class<T>
        out.name("type");
        out.value(value.type().getName());

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
        Class<T> type = null;

        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("value")) {
                if(in.peek() == JsonToken.NULL) {
                    in.nextNull();
                } else if (in.peek() == JsonToken.STRING) {
                    value = in.nextString();
                } else if (in.peek() == JsonToken.NUMBER) {
                    value = in.nextDouble();  // Use nextDouble() for general number handling
                } else if (in.peek() == JsonToken.BOOLEAN) {
                    value = in.nextBoolean();
                }
            } else if (name.equals("type")) {
                String className = in.nextString();
                try {
                    type = (Class<T>) Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("Class not found for type deserialization", e);
                }
            }
        }

        in.endObject();

        if (type == null) {
            throw new JsonParseException("Missing 'type' field");
        }


        // Convert value to the correct type if it's not null
        if (type == UUID.class && value != null) {
            assert value instanceof String;
            value = UUID.fromString((String) value);
        } else if (type.isEnum() && value != null) {
            assert value instanceof String;
            value = Enum.valueOf((Class<Enum>) type, (String) value);
        }

        return new Preference<>(type, type.cast(value));
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
