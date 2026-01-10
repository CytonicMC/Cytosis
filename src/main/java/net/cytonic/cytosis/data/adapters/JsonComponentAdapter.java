package net.cytonic.cytosis.data.adapters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.NoArgsConstructor;

import net.cytonic.cytosis.data.objects.JsonComponent;

@NoArgsConstructor
public class JsonComponentAdapter extends TypeAdapter<JsonComponent> {

    @Override
    public void write(JsonWriter out, JsonComponent value) throws IOException {
        out.beginObject();
        out.name("value");
        out.value(value.toJson());
        out.endObject();
    }

    @Override
    public JsonComponent read(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        String data = in.nextString();
        in.endObject();
        return JsonComponent.fromJson(data);
    }
}
