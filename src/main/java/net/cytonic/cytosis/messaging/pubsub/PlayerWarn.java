package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.containers.PlayerWarnContainer;
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
        PlayerWarnContainer container = PlayerWarnContainer.deserialize(message);
        UUID uuid = container.target();
        Component warnMessage = JSONComponentSerializer.json().deserialize(container.warnMessage());
        if (Cytosis.getPlayer(uuid).isPresent()) {
            Cytosis.getPlayer(uuid).get().sendMessage(warnMessage);
        }
    }
}
