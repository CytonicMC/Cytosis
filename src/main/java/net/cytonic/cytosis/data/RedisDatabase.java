package net.cytonic.cytosis.data;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.pubsub.*;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.entity.Player;
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
     * Cached player servers
     */
    public static final String ONLINE_PLAYER_SERVER_KEY = "online_player_server";

    /**
     * Player change servers channel
     */
    public static final String PLAYER_SERVER_CHANGE_CHANNEL = "player_server_change";
    /**
     * Player login/out channel
     */
    public static final String PLAYER_STATUS_CHANNEL = "player_status";
    /**
     * Server startup / shutdown
     */
    public static final String SERVER_STATUS_CHANNEL = "server_status";
    /**
     * Send player channel
     */
    public static final String SEND_PLAYER_CHANNEL = "player_send";
    /**
     * Chat channels channel
     */
    public static final String CHAT_CHANNELS_CHANNEL = "chat-channels";

    private final JedisPooled jedis;
    private final JedisPooled jedisPub;
    private final JedisPooled jedisSub;
    private final ExecutorService worker = Executors.newCachedThreadPool(Thread.ofVirtual().name("CytosisRedisWorker").factory());

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

        worker.submit(() -> jedisSub.subscribe(new PlayerLoginLogout(), PLAYER_STATUS_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new ServerStatus(), SERVER_STATUS_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new PlayerServerChange(), PLAYER_SERVER_CHANGE_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new ChatChannels(), CHAT_CHANNELS_CHANNEL));
    }

    /**
     * Sends a server shutdown message to the redis server
     */
    public void sendShutdownMessage() {
        // formatting: <START/STOP>|:|<SERVER_ID>|:|<SERVER_IP>|:|<SERVER_PORT>
        jedisPub.publish(SERVER_STATUS_CHANNEL, STR."STOP|:|\{Cytosis.SERVER_ID}|:|\{Utils.getServerIP()}|:|\{CytosisSettings.SERVER_PORT}");
        jedis.srem(ONLINE_SERVER_KEY, new CytonicServer(Utils.getServerIP(), Cytosis.SERVER_ID, CytosisSettings.SERVER_PORT).serialize());
        Logger.info("Server shutdown message sent!");
    }

    /**
     * Sends a server startup message to the redis server
     */
    public void sendStartupMessage() {
        // formatting: <START/STOP>|:|<SERVER_ID>|:|<SERVER_IP>|:|<SERVER_PORT>
        jedisPub.publish(SERVER_STATUS_CHANNEL, STR."START|:|\{Cytosis.SERVER_ID}|:|\{Utils.getServerIP()}|:|\{CytosisSettings.SERVER_PORT}");
        jedis.sadd(ONLINE_SERVER_KEY, new CytonicServer(Utils.getServerIP(), Cytosis.SERVER_ID, CytosisSettings.SERVER_PORT).serialize());
        Logger.info("Server startup message sent!");
    }

    public void sendPlayerToServer(Player player, CytonicServer server) {
        // formatting: <PLAYER_UUID>|:|<SERVER_ID>
        jedisPub.publish(SEND_PLAYER_CHANNEL, STR."\{player.getUuid()}|:|\{server.id()}");
    }

    /**
     * Sends a chat message to all servers
     * @param chatMessage the chat message
     * @param chatChannel the chat channel
     */
    public void sendChatMessage(Component chatMessage, ChatChannel chatChannel) {
        //formatting: {chat-message}|{chat-channel}
        String message = STR."\{JSONComponentSerializer.json().serialize(chatMessage)}|\{chatChannel.name()}";
        jedisPub.publish(CHAT_CHANNELS_CHANNEL, message);
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