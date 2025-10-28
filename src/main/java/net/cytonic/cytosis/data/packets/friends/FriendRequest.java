package net.cytonic.cytosis.data.packets.friends;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.packets.Packet;
import net.cytonic.cytosis.utils.InstantAdapter;

import java.time.Instant;
import java.util.UUID;

public record FriendRequest(UUID sender, UUID recipient, Instant expiry) implements Packet {
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX") // very important for interfacing with Go's time.Time
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .serializeNulls()
            .create();

    public static FriendRequest deserialize(byte[] data) {
        return GSON.fromJson(new String(data), FriendRequest.class);
    }

    /**
     * Serializes this friend request into a byte array
     *
     * @return the serialized byte array
     */
    public byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }

    /**
     * Serializes this friend request into a string
     *
     * @return the serialized string
     */
    public String asString() {
        return new String(serialize());
    }
}
