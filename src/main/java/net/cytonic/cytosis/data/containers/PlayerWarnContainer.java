package net.cytonic.cytosis.data.containers;

import net.cytonic.cytosis.Cytosis;

import java.util.UUID;

/**
 * The container for when a player gets warned
 *
 * @param target      the players uuid
 * @param warnMessage the warn message
 */
public record PlayerWarnContainer(UUID target, String warnMessage) {

    /**
     * Converts a serialized string into a {@link PlayerWarnContainer}
     *
     * @param json The serialized string
     * @return the container object
     */
    public static PlayerWarnContainer deserialize(String json) {
        return Cytosis.GSON.fromJson(json, PlayerWarnContainer.class);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    public String serialize() {
        return Cytosis.GSON.toJson(this);
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
