package net.cytonic.cytosis.data;

import lombok.Getter;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class that holds the connection to the redis cache
 */
public class RedisDatabase extends JedisPubSub {
    public static final String ONLINE_PLAYER_NAME_KEY = "online_player_names";
    public static final String ONLINE_PLAYER_UUID_KEY = "online_player_uuids";
    public static final String PLAYER_STATUS_CHANNEL = "player_status";
    public static final String SERVER_SHUTDOWN_KEY = "server_shutdown";
    private final Jedis jedis;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisRedisWorker").factory());
    @Getter
    private final Set<String> onlinePlayers;
    @Getter
    private final Set<UUID> onlineUUIDs;

    /**
     * Initializes the connection to redis using the loaded settings and the Jedis client
     */
    public RedisDatabase() {
        HostAndPort hostAndPort = new HostAndPort(CytosisSettings.REDIS_HOST, CytosisSettings.REDIS_PORT);
        JedisClientConfig config = DefaultJedisClientConfig.builder().password(CytosisSettings.REDIS_PASSWORD).build();
        this.jedis = new Jedis(hostAndPort, config);
        this.jedis.auth(CytosisSettings.REDIS_PASSWORD);


        onlinePlayers = jedis.smembers(ONLINE_PLAYER_NAME_KEY);
        Set<UUID> uuids = new HashSet<>();
        jedis.smembers(ONLINE_PLAYER_UUID_KEY).forEach(s -> uuids.add(UUID.fromString(s)));
        this.onlineUUIDs = uuids;
        Logger.info(STR."Loaded \{this.onlineUUIDs.size()} players.");
        worker.submit(() -> jedis.subscribe(this, PLAYER_STATUS_CHANNEL));
    }

    public void sendShutdownMessage() {
        jedis.set(SERVER_SHUTDOWN_KEY, "");
    }


    /**
     * Consumes messages on the redis pub/sub interface to determine the online players
     *
     * @param channel The channel that was messaged
     * @param message The connent of the message
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(PLAYER_STATUS_CHANNEL)) return;
        // <PLAYER_NAME>|:|<PLAYER_UUID>|:|<JOIN/LEAVE>
        String[] parts = message.split("\\|:\\|");
        if (parts[2].equalsIgnoreCase("JOIN")) {
            onlinePlayers.add(parts[0]);
            onlineUUIDs.add(UUID.fromString(parts[1]));
        } else {
            onlinePlayers.remove(parts[0]);
            onlineUUIDs.remove(UUID.fromString(parts[1]));
        }
    }

    public void disconnect() {
        jedis.disconnect();
    }
}
