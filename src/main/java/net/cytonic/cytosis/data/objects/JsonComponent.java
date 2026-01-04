package net.cytonic.cytosis.data.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.utils.Msg;

@Getter
@Setter
@AllArgsConstructor
public class JsonComponent {

    private Component component;

    public String toJson() {
        return Msg.toJson(component);
    }

    @Override
    public String toString() {
        return toJson();
    }

    public static JsonComponent fromJson(String string) {
        return new JsonComponent(Msg.fromJson(string));
    }
}
