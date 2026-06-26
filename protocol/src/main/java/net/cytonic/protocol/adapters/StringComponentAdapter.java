package net.cytonic.protocol.adapters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.NoArgsConstructor;

import net.cytonic.protocol.data.objects.StringComponent;

@NoArgsConstructor
public class StringComponentAdapter extends TypeAdapter<StringComponent> {

    @Override
    public void write(JsonWriter out, StringComponent value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value.toJson());
    }

    @Override
    public StringComponent read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        return StringComponent.fromJson(in.nextString());
    }
}
