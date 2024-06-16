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
@SuppressWarnings("unused")
public class RedisDatabase extends JedisPubSub {

    private final String ONLINE_PLAYER_NAME_KEY = "online_player_names";
    private final String ONLINE_PLAYER_UUID_KEY = "online_player_uuids";
    private final String PLAYER_STATUS_CHANNEL = "player_status";
    private final String SERVER_SHUTDOWN_KEY = "server_shutdown";
    private final JedisPooled jedis;
    private final ExecutorService worker = Executors.newCachedThreadPool(Thread.ofVirtual().name("CytosisRedisWorker").factory());
    @Getter
    private final Set<String> onlinePlayers;
    @Getter
    private final Set<UUID> onlineUUIDs;


    /**
     * Initializes the connection to redis using the loaded settings and the Jedis client
     */
    public RedisDatabase() {
        HostAndPort hostAndPort = new HostAndPort(CytosisSettings.REDIS_HOST, 6379);
        JedisClientConfig config = DefaultJedisClientConfig.builder().password(CytosisSettings.REDIS_PASSWORD).build();
        this.jedis = new JedisPooled(hostAndPort, config);

        onlinePlayers = jedis.smembers(ONLINE_PLAYER_NAME_KEY);
        Set<UUID> uuids = new HashSet<>();
        jedis.smembers(ONLINE_PLAYER_UUID_KEY).forEach(s -> uuids.add(UUID.fromString(s)));
        this.onlineUUIDs = uuids;
        Logger.info(STR."Loaded \{this.onlineUUIDs.size()} players.");
        worker.submit(() -> jedis.subscribe(this, PLAYER_STATUS_CHANNEL));
    }

    /**
     * Sends a server shutdown message to the redis server
     */
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

    /**
     * Disconnects from the redis server
     */
    public void disconnect() {
        worker.shutdown();
        jedis.close();
    }

    /**
     * Gets a set from the redis server
     *
     * @param key key
     * @return the set
     */
    public Set<String> getSet(String key) {
        return jedis.smembers(key);
    }

    /**
     * Set a key equal to a value
     *
     * @param key   key
     * @param value value
     */
    public void setValue(String key, String value) {
        jedis.set(key, value);
    }

    /**
     * Adds a value to a set
     *
     * @param key   key
     * @param value value(s)
     */
    public void addValue(String key, String... value) {
        jedis.sadd(key, value);
    }

    /**
     * Remove a value from a set
     *
     * @param key   key
     * @param value value(s)
     */
    public void removeValue(String key, String... value) {
        jedis.srem(key, value);
    }

    /**
     * Registers a pubsub listener
     *
     * @param jedisPubSub the class to listen to
     * @param channel     the channel to listen on
     */
    public void registerPubSub(JedisPubSub jedisPubSub, String channel) {
        worker.submit(() -> jedis.subscribe(jedisPubSub, channel));
    }

    /**
     * Publishes a message to the specified channel
     *
     * @param channel the channel
     * @param message the message
     */
    public void publish(String channel, String message) {
        jedis.publish(channel, message);
    }
}