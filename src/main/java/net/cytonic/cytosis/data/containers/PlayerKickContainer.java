package net.cytonic.cytosis.data.containers;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.KickReason;

import java.util.UUID;

/**
 * The container for when a player gets kicked
 *
 * @param uuid        the player's uuid
 * @param reason      the reason for the kick
 * @param kickMessage the message to send to the player when they are kicked
 */
public record PlayerKickContainer(UUID uuid, KickReason reason, String kickMessage) {

    /**
     * Converts a serialized string into a {@link PlayerKickContainer}
     *
     * @param json The serialized string
     * @return the container object
     */
    public static PlayerKickContainer deserialize(String json) {
        return Cytosis.GSON.fromJson(json, PlayerKickContainer.class);
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
