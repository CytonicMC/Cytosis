package net.cytonic.cytosis;

import lombok.Getter;
import net.cytonic.cytosis.data.RedisDatabase;

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

    /**
     * The default constructor
     */
    public CytonicNetwork() {
    }

    /**
     * Imports online player data from redis
     *
     * @param redisDatabase The redis instance
     */
    public void importDataFromRedis(RedisDatabase redisDatabase) {
        networkPlayers.clear();
        networkPlayerUUIDs.clear();
        networkPlayers.addAll(redisDatabase.getOnlinePlayers());
        networkPlayerUUIDs.addAll(redisDatabase.getOnlineUUIDs());
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