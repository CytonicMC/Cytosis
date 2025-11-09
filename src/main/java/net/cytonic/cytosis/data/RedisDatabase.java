package net.cytonic.cytosis.data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.config.CytosisSettings.RedisConfig;
import net.cytonic.cytosis.environments.EnvironmentManager;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;

/**
 * A class that holds the connection to the redis cache
 */
@CytosisComponent(dependsOn = {FileManager.class, EnvironmentManager.class})
public class RedisDatabase implements Bootstrappable {

    private final String prefix;

    /**
     * Cached global cooldowns
     */
    public static final String GLOBAL_COOLDOWNS_KEY = "global_cooldowns";

    private final JedisPooled jedis;
    private final ExecutorService worker = Executors.newCachedThreadPool(Thread.ofVirtual().name("CytosisRedisWorker")
        .uncaughtExceptionHandler(
            (throwable, runnable) -> Logger.error("An error occurred on the CytosisRedisWorker", throwable))
        .factory());

    /**
     * Initializes the connection to redis using the loaded settings and the Jedis client
     */
    public RedisDatabase() {
        RedisConfig settings = Cytosis.CONTEXT.getComponent(CytosisSettings.class).getRedisConfig();
        HostAndPort hostAndPort = new HostAndPort(settings.getHost(), settings.getPort());
        JedisClientConfig config = DefaultJedisClientConfig.builder().password(settings.getPassword()).build();
        this.jedis = new JedisPooled(hostAndPort, config);
        prefix = Cytosis.CONTEXT.getComponent(EnvironmentManager.class).getEnvironment().getPrefix();
    }

    @Override
    public void init() {
        Logger.info("Connected to Redis!");
    }

    /**
     * Disconnects from the redis server
     */
    @Override
    public void shutdown() {
        worker.shutdown();
        jedis.close();
        Logger.info("Disconnected from Redis!");
    }

    /**
     * Gets a set from the redis server
     *
     * @param key key
     * @return the set
     */
    public Set<String> getSet(String key) {
        return jedis.smembers(prefix + key);
    }

    /**
     * Set a key equal to a value
     *
     * @param key   key
     * @param value value
     */
    public void setValue(String key, String value) {
        jedis.set(prefix + key, value);
    }

    public String getValue(String key) {
        return jedis.get(prefix + key);
    }

    /**
     * Adds a value to a set
     *
     * @param key   key
     * @param value value(s)
     */
    public void addValue(String key, String... value) {
        jedis.sadd(prefix + key, value);
    }

    /**
     * Remove a value from a set
     *
     * @param key   key
     * @param value value(s)
     */
    public void removeValue(String key, String... value) {
        jedis.srem(prefix + key, value);
    }

    /**
     * Adds a key and value to a hash
     *
     * @param hash  the name of the hash
     * @param key   the key of the key value pair
     * @param value the value of the key value pair
     */
    public void addToHash(String hash, String key, String value) {
        jedis.hset(prefix + hash, key, value);
    }

    /**
     * Remove a key value pair from a hash
     *
     * @param hash the name of the hash
     * @param key  the field in the hash
     */
    public void removeFromHash(String hash, String key) {
        jedis.hdel(prefix + hash, key);
    }

    /**
     * Gets the map of key value pairs stored in a hash
     *
     * @param hash the key tied to the hash
     * @return the map of values
     */
    public Map<String, String> getHash(String hash) {
        return jedis.hgetAll(prefix + hash);
    }

    /**
     * Gets the specified field from the specified hash
     *
     * @param hash The hash to query
     * @param key  the field to query from the hash
     * @return the value stored in the hash
     */
    public String getFromHash(String hash, String key) {
        return jedis.hget(prefix + hash, key);
    }

    /**
     * Gets the keys associated with the specified pattern. For example, {@code foo*} would return {@code foooooo} and
     * {@code fooHiThisIsAKey}.
     * <br><strong>**This may be time-consuming, use sparingly if at all **</strong>
     *
     * @param pattern the pattern used to select the keys
     * @return the set of keys associated with the pattern
     */
    public Set<String> getKeys(String pattern) {
        return jedis.keys(pattern);
    }

    /**
     * Gets a set from the redis server
     *
     * @param key key
     * @return the set
     */
    public Set<String> getGlobalSet(String key) {
        return jedis.smembers(key);
    }

    /**
     * Set a key equal to a value
     *
     * @param key   key
     * @param value value
     */
    public void setGlobalValue(String key, String value) {
        jedis.set(key, value);
    }

    public String getGlobalValue(String key) {
        return jedis.get(key);
    }

    /**
     * Adds a value to a set
     *
     * @param key   key
     * @param value value(s)
     */
    public void addGlobalValue(String key, String... value) {
        jedis.sadd(key, value);
    }

    /**
     * Remove a value from a set
     *
     * @param key   key
     * @param value value(s)
     */
    public void removeGlobalValue(String key, String... value) {
        jedis.srem(key, value);
    }

    /**
     * Adds a key and value to a hash
     *
     * @param hash  the name of the hash
     * @param key   the key of the key value pair
     * @param value the value of the key value pair
     */
    public void addToGlobalHash(String hash, String key, String value) {
        jedis.hset(hash, key, value);
    }

    /**
     * Remove a key value pair from a hash
     *
     * @param hash the name of the hash
     * @param key  the field in the hash
     */
    public void removeFromGlobalHash(String hash, String key) {
        jedis.hdel(hash, key);
    }

    /**
     * Gets the map of key value pairs stored in a hash
     *
     * @param hash the key tied to the hash
     * @return the map of values
     */
    public Map<String, String> getGlobalHash(String hash) {
        return jedis.hgetAll(hash);
    }

    /**
     * Gets the specified field from the specified hash
     *
     * @param hash The hash to query
     * @param key  the field to query from the hash
     * @return the value stored in the hash
     */
    public String getFromGlobalHash(String hash, String key) {
        return jedis.hget(hash, key);
    }


}