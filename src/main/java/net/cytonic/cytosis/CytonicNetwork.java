package net.cytonic.cytosis;

import lombok.Getter;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.objects.PlayerServer;
import java.util.*;

/**
 * A class that holds data about the status of the Cytonic network
 */
@Getter
public class CytonicNetwork {
    private final Set<String> networkPlayers = new HashSet<>();
    private final Set<UUID> networkPlayerUUIDs = new HashSet<>();
    private final Map<String, CytonicServer> servers = new HashMap<>(); // online servers
    private final Map<String, PlayerServer> netoworkPlayersOnServers = new HashMap<>();
    private final Map<UUID, Boolean> serverAlerts = new HashMap<>();

    /**
     * The default constructor
     */
    public CytonicNetwork() {
    }

    /**
     * Imports online player data from redis
     *
     * @param redis The redis instance
     */
    public void importDataFromRedis(RedisDatabase redis) {
        networkPlayers.clear();
        networkPlayerUUIDs.clear();
        servers.clear();
        netoworkPlayersOnServers.clear();

        networkPlayers.addAll(redis.getSet(RedisDatabase.ONLINE_PLAYER_NAME_KEY));
        redis.getSet(RedisDatabase.ONLINE_PLAYER_UUID_KEY).forEach(s -> networkPlayerUUIDs.add(UUID.fromString(s)));
        redis.getSet(RedisDatabase.ONLINE_SERVER_KEY).forEach(s -> servers.put(CytonicServer.deserialize(s).id(), CytonicServer.deserialize(s)));
        redis.getSet(RedisDatabase.ONLINE_PLAYER_SERVER_KEY).forEach(s -> netoworkPlayersOnServers.put(s.split("\\|:\\|")[0], PlayerServer.deserialize(s)));
    }

    /**
     * Adds a player to the cache
     *
     * @param name The player's name
     * @param uuid The player's UUID
     */
    public void addPlayer(String name, UUID uuid) {
        networkPlayers.add(name);
        networkPlayerUUIDs.add(uuid);
    }

    /**
     * Removes the player from the cache
     *
     * @param name The player's name
     * @param uuid The player's UUID
     */
    public void removePlayer(String name, UUID uuid) {
        networkPlayers.remove(name);
        networkPlayerUUIDs.remove(uuid);
    }
}