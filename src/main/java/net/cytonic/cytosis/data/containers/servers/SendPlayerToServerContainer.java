package net.cytonic.cytosis.data.containers.servers;

import com.google.gson.Gson;

import java.util.UUID;

public record SendPlayerToServerContainer(UUID player, String serverId, UUID instance) {
    /**
     * Deserializes this object from a string
     *
     * @param json the serialized data
     * @return the deserailized object
     */
    public static SendPlayerToServerContainer deserialize(String json) {
        return new Gson().fromJson(json, SendPlayerToServerContainer.class);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    public String serialize() {
        return new Gson().toJson(this);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    @Override
    public String toString() {
        return serialize();
    }
}
