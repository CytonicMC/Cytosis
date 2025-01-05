package net.cytonic.cytosis.data.objects;

import com.google.gson.Gson;

/**
 * A class that holds data about a Cytosis server
 *
 * @param id   The server ID
 * @param ip   The ip address of the server
 *             (for example, 127.0.0.1)
 * @param port The port of the server, usually 25565
 */
@SuppressWarnings("unused")
public record CytonicServer(String ip, String id, int port) {

    /**
     * Converts a serialized string into a CytonicServer
     *
     * @param json The serialized string
     * @return the server object
     */
    public static CytonicServer deserialize(String json) {
        return new Gson().fromJson(json, CytonicServer.class);
    }

    /**
     * Serializes the server into a string
     *
     * @return the serialized string
     */
    public String serialize() {
        return new Gson().toJson(this);
    }

    /**
     * Serializes the server into a string
     *
     * @return the serialized string
     */
    @Override
    public String toString() {
        return serialize();
    }
}
