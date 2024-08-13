package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class PlayerMessage extends JedisPubSub {

    public PlayerMessage() {
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.PLAYER_MESSAGE_CHANNEL)) return;
        String[] strings = message.split("\\|:\\|");
        if (!Cytosis.getPlayer(UUID.fromString(strings[1])).isPresent()) return;
        Cytosis.getPlayer(UUID.fromString(strings[1])).get().sendMessage(JSONComponentSerializer.json().deserialize(strings[0]));
    }
}
