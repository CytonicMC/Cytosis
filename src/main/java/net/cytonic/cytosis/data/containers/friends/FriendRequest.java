package net.cytonic.cytosis.data.containers.friends;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cytonic.cytosis.utils.InstantAdapter;

import java.time.Instant;
import java.util.UUID;

public record FriendRequest(UUID sender, UUID recipient, Instant expiry) {
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX") // very important for interfacing with Go's time.Time
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .serializeNulls() // also allows us to send a null time.
            .create();


    public static FriendRequest deserialize(String json) {
        return GSON.fromJson(json, FriendRequest.class);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    public String serialize() {
        return GSON.toJson(this);
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
