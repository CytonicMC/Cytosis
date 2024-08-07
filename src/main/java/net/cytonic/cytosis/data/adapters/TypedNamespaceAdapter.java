package net.cytonic.cytosis.data.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.cytonic.objects.TypedNamespace;
import net.minestom.server.utils.NamespaceID;

import java.io.IOException;

public class TypedNamespaceAdapter extends TypeAdapter<TypedNamespace<?>> implements TypeAdapterFactory {
    @Override
    public void write(JsonWriter out, TypedNamespace<?> value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.beginObject();

        // Serialize NamespaceID
        out.name("namespaceID");
        out.value(value.namespaceID().asString());

        // Serialize Class<T>
        out.name("type");
        out.value(value.type().getName());

        out.endObject();
    }

    @Override
    public TypedNamespace<?> read(JsonReader in) throws IOException {
        in.beginObject();

        NamespaceID namespaceID = null;
        Class<?> type = null;

        while (in.hasNext()) {
            String name = in.nextName();
            if ("namespaceID".equals(name)) {
                // Deserialize NamespaceID
                String namespaceIDString = in.nextString();
                namespaceID = NamespaceID.from(namespaceIDString);
            } else if ("type".equals(name)) {
                // Deserialize Class<T>
                String className = in.nextString();
                try {
                    type = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new IOException(STR."Class not found: \{className}", e);
                }
            }
        }

        in.endObject();

        return new TypedNamespace<>(namespaceID, type);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!TypedNamespace.class.isAssignableFrom(type.getRawType())) {
            return null; // This factory does not handle this type
        }
        return (TypeAdapter<T>) gson.getDelegateAdapter(this, TypeToken.get(TypedNamespace.class));
    }
}
