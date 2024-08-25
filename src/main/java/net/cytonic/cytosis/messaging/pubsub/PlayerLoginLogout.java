package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/**
 * A pub sub that handles player login and logout
 */
public class PlayerLoginLogout extends JedisPubSub {

    /**
     * A default constructor
     */
    public PlayerLoginLogout() {
    }

    /**
     * Consumes messages on the redis pub/sub interface to determine the online players
     *
     * @param channel The channel that was messaged
     * @param message The content of the message
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.PLAYER_STATUS_CHANNEL)) return;
        CytonicNetwork network = Cytosis.getCytonicNetwork();
        if (network == null) return;
        // <PLAYER_NAME>|:|<PLAYER_UUID>|:|<JOIN/LEAVE>
        String[] parts = message.split("\\|:\\|");
        if (parts[2].equalsIgnoreCase("JOIN")) {
            network.addPlayer(parts[0], UUID.fromString(parts[1]));
            Cytosis.getPreferenceManager().loadPlayerPreferences(UUID.fromString(parts[1]));
            Cytosis.getFriendManager().sendLoginMessage(UUID.fromString(parts[1]));
        } else if (parts[2].equalsIgnoreCase("LEAVE")) {
            network.removePlayer(parts[0], UUID.fromString(parts[1]));
            Cytosis.getPreferenceManager().unloadPlayerPreferences(UUID.fromString(parts[1]));
            Cytosis.getFriendManager().sendLogoutMessage(UUID.fromString(parts[1]));
        }
    }
}
