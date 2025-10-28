package net.cytonic.cytosis.data.packets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

public record SnooperPacket(String rawMessage) implements Packet {

    public SnooperPacket(Component message) {
        this(JSONComponentSerializer.json().serialize(message));
    }

    public Component message() {
        return JSONComponentSerializer.json().deserialize(rawMessage);
    }
}
