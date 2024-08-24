package net.cytonic.cytosis.data;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.pubsub.*;
import net.cytonic.cytosis.utils.Utils;
import net.cytonic.enums.KickReason;
import net.cytonic.objects.ChatMessage;
import net.cytonic.objects.CytonicServer;
import net.cytonic.objects.OfflinePlayer;
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
     * Cached players.
     * <p>
     * Stored in a format consistent with {@link net.cytonic.objects.PlayerPair}
     */
    public static final String ONLINE_PLAYER_KEY = "online_players";
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
    public static final String CHAT_MESSAGES_CHANNEL = "chat-messages";
    /**
     * Broadcast channel
     */
    public static final String BROADCAST_CHANNEL = "broadcast";
    /**
     * Player message channel
     */
    public static final String PLAYER_MESSAGE_CHANNEL = "player-message";

    // friend requests
    /**
     * Send friend request
     */
    public static final String FRIEND_REQUEST_SENT = "friend-request-sent";
    /**
     * Published when a friend request expires
     */
    public static final String FRIEND_REQUEST_EXPIRED = "friend-request-expired";
    /**
     * Published when a friend request is declined
     */
    public static final String FRIEND_REQUEST_DECLINED = "friend-request-declined";
    /**
     * Published when a friend request is accepted
     */
    public static final String FRIEND_REQUEST_ACCEPTED = "friend-request-accepted";
    /**
     * Friend removed
     */
    public static final String FRIEND_REMOVED = "friend-removed";
    /**
     * Player kicked
     */
    public static final String PLAYER_KICK = "player-kick";

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

        worker.submit(() -> jedisSub.subscribe(new PlayerLoginLogout(), PLAYER_STATUS_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new ServerStatus(), SERVER_STATUS_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new PlayerServerChange(), PLAYER_SERVER_CHANGE_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new ChatMessages(), CHAT_MESSAGES_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new Broadcasts(), BROADCAST_CHANNEL));
        worker.submit(() -> jedisSub.subscribe(new Friends(), FRIEND_REQUEST_ACCEPTED, FRIEND_REQUEST_DECLINED, FRIEND_REQUEST_EXPIRED, FRIEND_REQUEST_SENT, FRIEND_REMOVED));
        worker.submit(() -> jedisSub.subscribe(new PlayerMessage(), PLAYER_MESSAGE_CHANNEL));
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

    /**
     * Sends a message to the redis server telling the proxies to move a player to a different server
     *
     * @param player The player to move
     * @param server the destination server
     */
    public void sendPlayerToServer(Player player, CytonicServer server) {
        // formatting: <PLAYER_UUID>|:|<SERVER_ID>
        jedisPub.publish(SEND_PLAYER_CHANNEL, STR."\{player.getUuid()}|:|\{server.id()}");
    }

    /**
     * Sends a chat message to all servers
     *
     * @param message the serialized message
     */
    public void sendChatMessage(ChatMessage message) {
        jedisPub.publish(CHAT_MESSAGES_CHANNEL, message.toJson());
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
     * Sends a message to a player
     *
     * @param message the serialized message
     */
    public void sendPlayerMessage(ChatMessage message) {
        jedisPub.publish(PLAYER_MESSAGE_CHANNEL, message.toString());
    }

    /**
     * Sends a message to Redis to kick a player.
     * <p>
     * Formatting: {@code {uuid}|:|{reason}|:|{name}|:|{message}|:|{rescuable}}
     *
     * @param player    The player to kick, on this server
     * @param reason    The reason for kicking the player
     * @param component The kick message displayed
     */
    public void kickPlayer(Player player, KickReason reason, Component component, Entry entry) {
        // FORMAT: {uuid}|:|{reason}|:|{name}|:|{message}|:|{rescuable}
        Cytosis.getDatabaseManager().getMysqlDatabase().addAuditLogEntry(entry);
        String message = STR."\{player.getUuid()}|:|\{reason}|:|\{player.getUsername()}|:|\{JSONComponentSerializer.json().serialize(component)}|:|\{reason.isRescuable()}";
        jedisPub.publish(PLAYER_KICK, message);
    }

    /**
     * Sends a message to RabbitMQ to kick a player.
     * <p>
     * Formatting: {@code {uuid}|:|{reason}|:|{name}|:|{message}|:|{rescuable}}
     *
     * @param player    The player to kick, on another server
     * @param reason    The reason for kicking the player
     * @param component The kick message displayed
     */
    public void kickPlayer(OfflinePlayer player, KickReason reason, Component component, Entry entry) {
        // FORMAT: {uuid}|:|{reason}|:|{name}|:|{message}|:|{rescuable}
        Cytosis.getDatabaseManager().getMysqlDatabase().addAuditLogEntry(entry);
        String message = STR."\{player.uuid()}|:|\{reason}|:|\{player.name()}|:|\{JSONComponentSerializer.json().serialize(component)}|:|\{reason.isRescuable()}";
        jedisPub.publish(PLAYER_KICK, message);
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