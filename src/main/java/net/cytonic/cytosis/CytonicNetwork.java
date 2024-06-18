package net.cytonic.cytosis;

import lombok.Getter;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.objects.CytonicServer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A class that holds data about the status of the Cytonic network
 */
@Getter
public class CytonicNetwork {
    private final Set<String> networkPlayers = new HashSet<>();
    private final Set<UUID> networkPlayerUUIDs = new HashSet<>();

    private final Set<CytonicServer> servers = new HashSet<>(); // online servers

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

        networkPlayers.addAll(redis.getSet(RedisDatabase.ONLINE_PLAYER_NAME_KEY));
        redis.getSet(RedisDatabase.ONLINE_PLAYER_UUID_KEY).forEach(s -> networkPlayerUUIDs.add(UUID.fromString(s)));
        redis.getSet(RedisDatabase.ONLINE_SERVER_KEY).forEach(s -> servers.add(CytonicServer.deserialize(s)));
    }

    /**
     * Adds a player to the cache
     * @param name The player's name
     * @param uuid The player's UUID
     */
    public void addPlayer(String name, UUID uuid) {
        networkPlayers.add(name);
        networkPlayerUUIDs.add(uuid);
    }

    /**
     * Removes the player from the cache
     * @param name The player's name
     * @param uuid The player's UUID
     */
    public void removePlayer(String name, UUID uuid) {
        networkPlayers.remove(name);
        networkPlayerUUIDs.remove(uuid);
    }
}