package net.cytonic.cytosis.data;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.containers.PlayerWarnContainer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.pubsub.Broadcasts;
import net.cytonic.cytosis.messaging.pubsub.Cooldowns;
import net.cytonic.cytosis.messaging.pubsub.PlayerWarn;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import redis.clients.jedis.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class that holds the connection to the redis cache
 */
public class RedisDatabase {


    /**
     * Cached global cooldowns
     */
    public static final String GLOBAL_COOLDOWNS_KEY = "global_cooldowns";

    /**
     * Cooldown pubsub
     */
    public static final String COOLDOWN_UPDATE_CHANNEL = "update_cooldowns";


    /**
     * Broadcast channel
     */
    public static final String BROADCAST_CHANNEL = "broadcast";

    /**
     * Player warn
     */
    public static final String PLAYER_WARN = "player-warn";

    private final JedisPooled jedis;
    private final JedisPooled jedisPub;
    private final JedisPooled jedisSub;
    private final ExecutorService worker = Executors.newCachedThreadPool(Thread.ofVirtual().name("CytosisRedisWorker")
            .uncaughtExceptionHandler((throwable, runnable) -> Logger.error("An error occurred on the CytosisRedisWorker", throwable)).factory());

    /**
     * Initializes the connection to redis using the loaded settings and the Jedis client
     */
    public RedisDatabase() {
        HostAndPort hostAndPort = new HostAndPort(CytosisSettings.REDIS_HOST, 6379);
        JedisClientConfig config = DefaultJedisClientConfig.builder().password(CytosisSettings.REDIS_PASSWORD).build();
        this.jedis = new JedisPooled(hostAndPort, config);
        this.jedisPub = new JedisPooled(hostAndPort, config);
        this.jedisSub = new JedisPooled(hostAndPort, config);
        Logger.info("Connected to Redis!");

        worker.submit(() -> jedisSub.subscribe(new Broadcasts(), BROADCAST_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new Cooldowns(), COOLDOWN_UPDATE_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new PlayerWarn(), PLAYER_WARN));
    }

    /**
     * Sends a broadcast to all servers
     *
     * @param broadcast the broadcast
     */
    public void sendBroadcast(Component broadcast) {
        String message = JSONComponentSerializer.json().serialize(broadcast);
        jedisPub.publish(BROADCAST_CHANNEL, message);
    }

    /**
     * Sends a message to Redis to warn a player.
     * <p>
     *
     * @param target      the player to warn
     * @param actor       the actor
     * @param warnMessage the message to warn the player with
     * @param reason      the reason
     * @param entry       the audit log entry
     */
    public void warnPlayer(UUID target, UUID actor, Component warnMessage, String reason, Entry entry) {
        Cytosis.getDatabaseManager().getMysqlDatabase().addAuditLogEntry(entry);
        Cytosis.getDatabaseManager().getMysqlDatabase().addPlayerWarn(actor, target, reason);
        PlayerWarnContainer container = new PlayerWarnContainer(target, JSONComponentSerializer.json().serialize(warnMessage));
        jedisPub.publish(PLAYER_WARN, container.toString());
    }

    /**
     * Disconnects from the redis server
     */
    public void disconnect() {
        worker.shutdown();
        jedis.close();
        jedisPub.close();
        jedisSub.close();
        Logger.info("Disconnected from Redis!");
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

    /**
     * Adds a key and value to a hash
     *
     * @param hash  the name of the hash
     * @param key   the key of the key value pair
     * @param value the value of the key value pair
     */
    public void addToHash(String hash, String key, String value) {
        jedis.hset(hash, key, value);
    }

    /**
     * Remove a key value pair from a hash
     *
     * @param key   the name of the hash
     * @param field the field in the hash
     */
    public void removeFromHash(String key, String field) {
        jedis.hdel(key, field);
    }

    /**
     * Gets the map of key value pairs stored in a hash
     *
     * @param key the key tied to the hash
     * @return the map of values
     */
    public Map<String, String> getHash(String key) {
        return jedis.hgetAll(key);
    }

    /**
     * Gets the specified field from the specified hash
     *
     * @param key   The hash to query
     * @param field the field to query from the hash
     * @return the value stored in the hash
     */
    public String getFromHash(String key, String field) {
        return jedis.hget(key, field);
    }

    /**
     * Gets the keys associated with the specified pattern. For example, {@code foo*} would return {@code foooooo} and {@code fooHiThisIsAKey}.
     * <br><strong>**This may be time consuming, use sparingly if at all **</strong>
     *
     * @param pattern the pattern used to select the keys
     * @return the set of keys associated with the pattern
     */
    public Set<String> getKeys(String pattern) {
        return jedis.keys(pattern);
    }
}