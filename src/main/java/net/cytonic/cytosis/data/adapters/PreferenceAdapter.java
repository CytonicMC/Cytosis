package net.cytonic.cytosis.data.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.objects.Preference;

import java.io.IOException;
import java.util.UUID;

public class PreferenceAdapter<T> extends TypeAdapter<Preference<T>> implements TypeAdapterFactory {
    @Override
    public void write(JsonWriter out, Preference<T> value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        out.name("value");
        if (value.value() == null) {
            out.nullValue();
        } else {
            Cytosis.GSON.toJson(value.value(), value.value().getClass(), out);
        }
        out.endObject();
    }

    @Override
    public Preference<T> read(JsonReader in) throws IOException {
        in.beginObject();
        T value = null;

        while (in.hasNext()) {
            String name = in.nextName();
            if ("value".equals(name)) {
                value = Cytosis.GSON.fromJson(in, UUID.class);
            }
        }

        in.endObject();
        return new Preference<>(value);
    }

    @Override
    public <P> TypeAdapter<P> create(Gson gson, TypeToken<P> type) {
        if (type.getRawType() == Preference.class) {
            return (TypeAdapter<P>) new PreferenceAdapter<>().nullSafe();
        }
        return null;
    }
}
