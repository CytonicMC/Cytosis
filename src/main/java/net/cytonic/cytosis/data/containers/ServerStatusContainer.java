package net.cytonic.cytosis.data.containers;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.utils.InstantAdapter;

// type serves as a group now
public record ServerStatusContainer(String type, String ip, String id, int port, @Nullable Instant last_seen,
                                    String group) {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantAdapter())
        .serializeNulls() // also allows us to send a null time.
        .create();

    public static ServerStatusContainer deserialize(String json) {
        return GSON.fromJson(json, ServerStatusContainer.class);
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

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    public String serialize() {
        return GSON.toJson(this);
    }

    /**
     * Creates a new {@link CytonicServer} object using the server's IP, name, and port.
     *
     * @return A new {@link CytonicServer} object representing the server.
     * @see CytonicServer
     */
    public CytonicServer server() {
        return new CytonicServer(ip, id, port, type, group);
    }
}
