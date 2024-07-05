package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import redis.clients.jedis.JedisPubSub;
import java.util.UUID;

public class Messages extends JedisPubSub {

    public Messages() {}

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.MESSAGE_CHANNEL)) {
            //formatting {message}|:|{target_uuid}
            Logger.debug("got message!");
            String[] parts = message.split("\\|:\\|");
            Component msg = JSONComponentSerializer.json().deserialize(parts[0]);
            UUID target = UUID.fromString(parts[1]);
            if (Cytosis.getPlayer(target).isPresent()) Cytosis.getPlayer(target).get().sendMessage(msg);

        }
    }
}
