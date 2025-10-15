package net.cytonic.cytosis.data.objects;

import java.util.UUID;

import net.cytonic.cytosis.Cytosis;

/**
 * A class that holds a pair of UUID and name, mostly used to store players in Redis
 *
 * @param uuid The player's UUID
 * @param name The player's name
 */
@SuppressWarnings("unused")
public record PlayerPair(UUID uuid, String name) {

    /**
     * Converts a json representation of this object into a player pair
     *
     * @param json the json string
     * @return the player pair
     */
    public static PlayerPair deserialize(String json) {
        return Cytosis.GSON.fromJson(json, PlayerPair.class);
    }

    /**
     * Converts the player pair into a JSON string
     * <p>
     * This method is an alias for {@link #toString()}
     *
     * @return the json string
     */
    public String serialize() {
        return toString();
    }

    /**
     * Converts the player pair into a JSON string
     *
     * @return the json string
     */
    @Override
    public String toString() {
        return Cytosis.GSON.toJson(this);
    }
}
