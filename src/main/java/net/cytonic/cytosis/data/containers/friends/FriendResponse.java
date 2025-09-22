package net.cytonic.cytosis.data.containers.friends;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Cytosis;

// CHECKSTYLE:OFF: ParameterName
public record FriendResponse(UUID request_id) {
// CHECKSTYLE:ON: ParameterName

    /**
     * Deserializes this object from a string
     *
     * @param json the serialized data
     * @return the serialized object
     */
    public static FriendResponse deserialize(String json) {
        return Cytosis.GSON.fromJson(json, FriendResponse.class);
    }

    public static byte[] create(UUID requestId) {
        return new FriendResponse(requestId).serialize().getBytes(StandardCharsets.UTF_8);
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
    public @NotNull String toString() {
        return serialize();
    }
}
