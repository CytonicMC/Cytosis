package net.cytonic.cytosis.data.containers.friends;

import net.cytonic.cytosis.Cytosis;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A record class that is a json "packet", representing accepting a friend request with 2 UUIDs.
 */
public record OrganicFriendResponse(UUID sender, UUID recipient) {
    /**
     * Deserializes this object from a string
     *
     * @param json the serialized data
     * @return the deserailized object
     */
    public static OrganicFriendResponse deserialize(String json) {
        return Cytosis.GSON.fromJson(json, OrganicFriendResponse.class);
    }

    public static byte[] create(UUID sender, UUID recipient) {
        return new OrganicFriendResponse(sender, recipient).serialize().getBytes(StandardCharsets.UTF_8);
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
