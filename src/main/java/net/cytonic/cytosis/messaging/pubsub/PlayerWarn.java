package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class PlayerWarn extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.PLAYER_WARN)) return;
        //FORMATTING {uuid}|:|{warn_message}
        String[] parts = message.split("\\|:\\|");
        UUID uuid = UUID.fromString(parts[0]);
        Component warnMessage = JSONComponentSerializer.json().deserialize(parts[1]);
        if (Cytosis.getPlayer(uuid).isPresent()) {
            Cytosis.getPlayer(uuid).get().sendMessage(warnMessage);
        }
    }
}
