package net.cytonic.protocol.data.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

@Getter
@Setter
@AllArgsConstructor
public class StringComponent {

    private Component component;

    public static StringComponent fromJson(String string) {
        Component comp;
        try {
            comp = JSONComponentSerializer.json().deserialize(string);
        } catch (Exception e) {
            comp = MiniMessage.miniMessage().deserialize(string);
        }
        return new StringComponent(comp);
    }

    public String toJson() {
        return JSONComponentSerializer.json().serialize(component);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
