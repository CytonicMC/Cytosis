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

    public void importDataFromRedis(RedisDatabase redisDatabase) {
        networkPlayers.clear();
        networkPlayerUUIDs.clear();
        networkPlayers.addAll(redisDatabase.getOnlinePlayers());
        networkPlayerUUIDs.addAll(redisDatabase.getOnlineUUIDs());
    }

    public void addPlayer(String name, UUID uuid) {
        networkPlayers.add(name);
        networkPlayerUUIDs.add(uuid);
    }

    public void removePlayer(String name, UUID uuid) {
        networkPlayers.remove(name);
        networkPlayerUUIDs.remove(uuid);
    }
}