package net.cytonic.cytosis.data.objects;

import com.google.gson.Gson;

import java.util.UUID;

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
        return new Gson().fromJson(json, PlayerPair.class);
    }

    /**
     * Converts the player pair into a JSON string
     *
     * @return the json string
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
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
}
