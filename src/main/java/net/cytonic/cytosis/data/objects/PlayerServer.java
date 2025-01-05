package net.cytonic.cytosis.data.objects;

import com.google.gson.Gson;

import java.util.UUID;

/**
 * Represents a player connected to a specific server.
 *
 * @param uuid   The uuid of the player.
 * @param server The server the player is connected to.
 */
public record PlayerServer(UUID uuid, CytonicServer server) {

    /**
     * Converts a serialized string into a {@link PlayerServer}
     *
     * @param json The serialized string
     * @return the playerServer object
     */
    public static PlayerServer deserialize(String json) {
        return new Gson().fromJson(json, PlayerServer.class);
    }

    /**
     * Serializes the playerServerinto a string
     *
     * @return the serialized string
     */
    public String serialize() {
        return new Gson().toJson(this);
    }

    /**
     * Serializes the playerServer into a string
     *
     * @return the serialized string
     */
    @Override
    public String toString() {
        return serialize();
    }
}
