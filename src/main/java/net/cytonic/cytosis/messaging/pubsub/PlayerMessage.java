package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.objects.ChatMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;

public class PlayerMessage extends JedisPubSub {

    public PlayerMessage() {
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.PLAYER_MESSAGE_CHANNEL)) return;
        ChatMessage chatMessage = ChatMessage.fromJson(message);
        Objects.requireNonNull(chatMessage.recipients()).forEach(uuid -> {
            if (Cytosis.getPlayer(uuid).isEmpty()) return;
            Cytosis.getPlayer(uuid).get().sendMessage(JSONComponentSerializer.json().deserialize(chatMessage.serializedMessage()));
        });
    }
}
