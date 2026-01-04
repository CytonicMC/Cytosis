package net.cytonic.cytosis.data.adapters;

import java.io.IOException;
import java.time.Instant;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class InstantAdapter extends TypeAdapter<Instant> {


    /**
     * Writes one JSON value (an array, object, string, number, boolean or null) for {@code value}.
     *
     * @param value the Java object to write. May be null.
     */
    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    /**
     * Reads one JSON value (an array, object, string, number, boolean or null) and converts it to a Java object.
     * Returns the converted object.
     *
     * @return the converted Java object. May be {@code null}.
     */
    @Override
    public Instant read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String value = in.nextString();
        return Instant.parse(value);
    }
}
