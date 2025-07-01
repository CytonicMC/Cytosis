package net.cytonic.cytosis.data.containers;

import net.cytonic.cytosis.Cytosis;

import java.util.UUID;

/**
 * The container for when the player logs in or logs out
 *
 * @param username the username of the player
 * @param uuid     the uuid of the player
 */
public record PlayerLoginLogoutContainer(String username, UUID uuid) {

    /**
     * Converts a serialized string into a {@link PlayerLoginLogoutContainer}
     *
     * @param json The serialized string
     * @return the container object
     */
    public static PlayerLoginLogoutContainer deserialize(String json) {
        return Cytosis.GSON.fromJson(json, PlayerLoginLogoutContainer.class);
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
