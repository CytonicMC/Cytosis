package net.cytonic.cytosis.data.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

public class InstantAdapter extends TypeAdapter<Instant> {
    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        out.beginObject();
        out.name("value");
        out.value(value.toString());
        out.endObject();
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        String value = in.nextString();
        in.endObject();
        return Instant.parse(value);
    }
}
