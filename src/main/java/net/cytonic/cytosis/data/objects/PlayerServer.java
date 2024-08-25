package net.cytonic.cytosis.data.objects;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.objects.CytonicServer;

import java.util.UUID;

/**
 * An object representing a player and their server
 *
 * @param playerName the player's name
 * @param playerUUID the player's UUID
 * @param server     the player's current server
 */
public record PlayerServer(String playerName, UUID playerUUID, CytonicServer server) {

    /**
     * Deserializes the player server from a json string
     * @param serialized the serialized string
     * @return the player server object
     */
    public static PlayerServer deserialize(String serialized) {
        //<PLAYER_NAME>|:|<PLAYER_UUID>|:|<OLD_SERVER_NAME/NEW_SERVER_NAME>
        String[] parts = serialized.split("\\|:\\|");
        return new PlayerServer(parts[0], UUID.fromString(parts[1]), Cytosis.getCytonicNetwork().getServers().get(parts[2]));
    }
}
