package net.cytonic.cytosis.data.packets;

import com.google.common.base.Preconditions;
import com.google.gson.JsonParseException;
import net.cytonic.cytosis.Cytosis;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet that can be serialized and deserialized
 */
public interface Packet {

    /**
     * Deserializes a string into a {@link Packet}.
     *
     * @param data  the string
     * @param clazz the class of the packet
     * @param <T>   the packet type
     * @return the packet object
     */
    @NotNull
    static <T extends Packet> T deserialize(@NotNull String data, @NotNull Class<T> clazz) {
        Preconditions.checkNotNull(data, "data");
        Preconditions.checkNotNull(data, "clazz");
        return deserialize(data.getBytes(), clazz);
    }

    /**
     * Deserializes a byte array into a {@link Packet}
     *
     * @param data  the byte array
     * @param clazz the class of the packet
     * @param <T>   the packet type
     * @return the packet object
     */
    @NotNull
    static <T extends Packet> T deserialize(byte[] data, @NotNull Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "clazz");
        String json = new String(data);
        try {
            T packet = Cytosis.GSON.fromJson(json, clazz);
            if (packet == null) {
                throw new JsonParseException("Received invalid packet " + clazz.getSimpleName() + " '" + json + "'");
            }
            return Cytosis.GSON.fromJson(json, clazz);
        } catch (Exception e) {
            throw new JsonParseException("Received invalid packet " + clazz.getSimpleName() + " '" + json + "'", e);
        }
    }

    /**
     * Serializes this packet into a byte array
     *
     * @return the serialized byte array
     */
    default byte[] serialize() {
        return Cytosis.GSON.toJson(this).getBytes();
    }

    /**
     * Serializes this packet into a string
     *
     * @return the serialized string
     */
    default String asString() {
        return new String(serialize());
    }
}
