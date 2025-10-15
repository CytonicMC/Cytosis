package net.cytonic.cytosis.data.containers.snooper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

import net.cytonic.cytosis.Cytosis;

public record SnooperContainer(String rawMessage) {

    public SnooperContainer(Component message) {
        this(JSONComponentSerializer.json().serialize(message));
    }

    public static SnooperContainer deserialize(byte[] data) {
        return Cytosis.GSON.fromJson(new String(data), SnooperContainer.class);
    }

    /**
     * Syntax sugar to directly create a byte array from a component
     *
     * @param message The snoop message
     * @return the serialized byte array
     */
    public static byte[] pipeline(Component message) {
        return new SnooperContainer(message).serialize();
    }

    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }

    public Component message() {
        return JSONComponentSerializer.json().deserialize(rawMessage);
    }
}
