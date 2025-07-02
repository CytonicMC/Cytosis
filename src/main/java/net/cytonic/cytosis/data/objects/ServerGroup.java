package net.cytonic.cytosis.data.objects;

import net.cytonic.cytosis.Cytosis;


/**
 * A record representing a server group
 *
 * @param group the group of the server group
 * @param type  the map name of the server group
 */
public record ServerGroup(String group, String type) {

    /**
     * Deserializes the given json into a {@link ServerGroup}
     *
     * @param json The raw json string
     * @return the deserialized {@link ServerGroup}
     **/
    public static ServerGroup deserialize(String json) {
        return Cytosis.GSON.fromJson(json, ServerGroup.class);
    }

    /**
     * Serializes the server group into a json string
     *
     * @return the serialized string
     */
    public String serialize() {
        return Cytosis.GSON.toJson(this);
    }

    /**
     * Serializes the server group into a json string
     *
     * @return the serialized string
     */
    @Override
    public String toString() {
        return serialize();
    }

    public String humanReadable() {
        return group + ":" + type;
    }

}
