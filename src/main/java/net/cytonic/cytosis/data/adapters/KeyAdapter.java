package net.cytonic.cytosis.data.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;

import java.io.IOException;

@NoArgsConstructor
public class KeyAdapter extends TypeAdapter<Key> implements TypeAdapterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(JsonWriter out, Key value) throws IOException {
        out.beginObject();
        out.name("key");
        out.value(value.asString());
        out.endObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Key read(JsonReader in) throws IOException {
        in.beginObject();

        String key = null;

        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("key")) {
                key = in.nextString();
            }
        }

        in.endObject();

        if (key == null) throw new JsonParseException("Preference deserialization failed: No group found");

        return Key.key(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (!Key.class.isAssignableFrom(type.getRawType())) {
            return null; // This factory does not handle this type
        }
        return (TypeAdapter<R>) gson.getDelegateAdapter(this, TypeToken.get(Key.class));
    }
}
