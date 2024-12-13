package net.cytonic.cytosis.messaging.pubsub;

import lombok.NoArgsConstructor;
import net.cytonic.containers.PlayerLoginLogoutContainer;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import redis.clients.jedis.JedisPubSub;

/**
 * A pub sub that handles player login and logout
 */
@NoArgsConstructor
public class PlayerLoginLogout extends JedisPubSub {

    /**
     * Consumes messages on the redis pub/sub interface to determine the online players
     *
     * @param channel The channel that was messaged
     * @param message The content of the message
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.PLAYER_STATUS_CHANNEL)) return;
        PlayerLoginLogoutContainer container = PlayerLoginLogoutContainer.deserialize(message);
        if (container.type().equals(PlayerLoginLogoutContainer.Type.LOGIN)) {
            Cytosis.getCytonicNetwork().addPlayer(container.username(), container.uuid());
            Cytosis.getPreferenceManager().loadPlayerPreferences(container.uuid());
            Cytosis.getFriendManager().sendLoginMessage(container.uuid());
        } else if (container.type().equals(PlayerLoginLogoutContainer.Type.LOGOUT)) {
            Cytosis.getCytonicNetwork().removePlayer(container.username(), container.uuid());
            Cytosis.getPreferenceManager().unloadPlayerPreferences(container.uuid());
            Cytosis.getFriendManager().sendLogoutMessage(container.uuid());
        }
    }
}
