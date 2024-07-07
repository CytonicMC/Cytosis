package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import redis.clients.jedis.JedisPubSub;

/**
 * A pub sub that handles broadcasts
 */
public class Broadcasts extends JedisPubSub {

    /**
     * A default constructor
     */
    public Broadcasts() {
    }

    /**
     * Consumes messages on the redis pub/sub interface to receive broadcasts
     *
     * @param channel The channel that was messaged
     * @param message The content of the message
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.BROADCAST_CHANNEL)) return;
        Cytosis.getOnlinePlayers().forEach(player -> player.sendMessage(JSONComponentSerializer.json().deserialize(message)));
    }
}
