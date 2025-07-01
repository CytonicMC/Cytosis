package net.cytonic.cytosis.data.objects;

import net.cytonic.cytosis.Cytosis;

/**
 * A class that holds data about a Cytosis server
 *
 * @param id    The server ID
 * @param ip    The ip address of the server
 *              (for example, 127.0.0.1)
 * @param port  The port of the server, usually 25565
 * @param type  The server type of the server
 * @param group The server group of the server
 */
@SuppressWarnings("unused")
public record CytonicServer(String ip, String id, int port, String type, String group) {

    /**
     * Converts a serialized string into a CytonicServer
     *
     * @param json The serialized string
     * @return the server object
     */
    public static CytonicServer deserialize(String json) {
        return Cytosis.GSON.fromJson(json, CytonicServer.class);
    }

    /**
     * Serializes the server into a string
     *
     * @return the serialized string
     */
    public String serialize() {
        return Cytosis.GSON.toJson(this);
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

    /**
     * Gets the server group
     *
     * @return the server group
     */
    public ServerGroup getServerGroup() {
        return new ServerGroup(group, type);
    }
}
