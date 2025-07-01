package net.cytonic.cytosis.data.containers.servers;

import net.cytonic.cytosis.Cytosis;

import java.util.UUID;

public record FindPlayerContainer(UUID player) {
    /**
     * Deserializes this object from a string
     *
     * @param json the serialized data
     * @return the deserailized object
     */
    public static FindPlayerContainer deserialize(String json) {
        return Cytosis.GSON.fromJson(json, FindPlayerContainer.class);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    public byte[] serialize() {
        return toString().getBytes();
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    @Override
    public String toString() {
        return Cytosis.GSON.toJson(this);
    }
}
