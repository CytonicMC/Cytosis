package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.messaging.containers.Container;
import net.cytonic.cytosis.messaging.containers.CooldownUpdateContainer;
import redis.clients.jedis.JedisPubSub;

public class Cooldowns extends JedisPubSub {
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.COOLDOWN_UPDATE_CHANNEL)) return;
        CooldownUpdateContainer container = (CooldownUpdateContainer) Container.deserialize(message);
        if (container.getTarget() == CooldownUpdateContainer.CooldownTarget.PERSONAL) {
            Cytosis.getNetworkCooldownManager().setPersonal(container.getUserUuid(), container.getNamespace(), container.getExpiry());
        } else if (container.getTarget() == CooldownUpdateContainer.CooldownTarget.GLOBAL) {
            Cytosis.getNetworkCooldownManager().setGlobal(container.getNamespace(), container.getExpiry());
        } else throw new IllegalArgumentException(STR."Unsupported target: \{container.getTarget()}");
    }
}
