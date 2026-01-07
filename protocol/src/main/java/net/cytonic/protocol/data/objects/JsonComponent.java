package net.cytonic.protocol.data.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

@Getter
@Setter
@AllArgsConstructor
public class JsonComponent {

    private Component component;

    public String toJson() {
        return JSONComponentSerializer.json().serialize(component);
    }

    @Override
    public String toString() {
        return toJson();
    }

    public static JsonComponent fromJson(String string) {
        return new JsonComponent(JSONComponentSerializer.json().deserialize(string));
    }
}
