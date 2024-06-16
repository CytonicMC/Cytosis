package net.cytonic.cytosis.data;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.pubsub.PlayerLoginLogout;
import net.cytonic.cytosis.messaging.pubsub.ServerStatus;
import net.cytonic.cytosis.utils.Utils;
import redis.clients.jedis.*;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class that holds the connection to the redis cache
 */
@SuppressWarnings("unused")
public class RedisDatabase {

    /**
     * Cached player names
     */
    public static final String ONLINE_PLAYER_NAME_KEY = "online_player_names";
    /**
     * Cached player UUIDs
     */
    public static final String ONLINE_PLAYER_UUID_KEY = "online_player_uuids";
    /**
     * Cached Servers
     */
    public static final String ONLINE_SERVER_KEY = "online_servers";

    /**
     * Player login/out channel
     */
    public static final String PLAYER_STATUS_CHANNEL = "player_status";
    /**
     * Server startup / shutdown
     */
    public static final String SERVER_STATUS_CHANNEL = "server_status";

    private final JedisPooled jedis;
    private final JedisPooled jedisPub;
    private final JedisPooled jedisSub;
    private final ExecutorService worker = Executors.newCachedThreadPool(Thread.ofVirtual().name("CytosisRedisWorker").factory());

    // server cache


    /**
     * Initializes the connection to redis using the loaded settings and the Jedis client
     */
    public RedisDatabase() {
        HostAndPort hostAndPort = new HostAndPort(CytosisSettings.REDIS_HOST, 6379);
        JedisClientConfig config = DefaultJedisClientConfig.builder().password(CytosisSettings.REDIS_PASSWORD).build();
        this.jedis = new JedisPooled(hostAndPort, config);
        this.jedisPub = new JedisPooled(hostAndPort, config);
        this.jedisSub = new JedisPooled(hostAndPort, config);

        worker.submit(() -> jedisSub.subscribe(new PlayerLoginLogout(), PLAYER_STATUS_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new ServerStatus(), SERVER_STATUS_CHANNEL));
    }

    /**
     * Sends a server shutdown message to the redis server
     */
    public void sendShutdownMessage() {
        // formatting: <START/STOP>|:|<SERVER_ID>|:|<SERVER_IP>|:|<SERVER_PORT>
        jedisPub.publish(SERVER_STATUS_CHANNEL, STR."STOP|:|\{Cytosis.SERVER_ID}|:|\{Utils.getServerIP()}|:|\{CytosisSettings.SERVER_PORT}");
        Logger.info("Server shutdown message sent!");
    }

    /**
     * Sends a server startup message to the redis server
     */
    public void sendStartupMessage() {
        // formatting: <START/STOP>|:|<SERVER_ID>|:|<SERVER_IP>|:|<SERVER_PORT>
        jedisPub.publish(SERVER_STATUS_CHANNEL, STR."START|:|\{Cytosis.SERVER_ID}|:|\{Utils.getServerIP()}|:|\{CytosisSettings.SERVER_PORT}");
        Logger.info("Server startup message sent!");
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
        worker.submit(() -> jedisSub.subscribe(jedisPubSub, channel));
    }

    /**
     * Publishes a message to the specified channel
     *
     * @param channel the channel
     * @param message the message
     */
    public void publish(String channel, String message) {
        jedisPub.publish(channel, message);
    }
}