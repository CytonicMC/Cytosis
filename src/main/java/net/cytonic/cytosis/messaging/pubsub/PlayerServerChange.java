package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.objects.PlayerServer;
import redis.clients.jedis.JedisPubSub;
import java.util.UUID;

public class PlayerServerChange extends JedisPubSub {

    /**
     * Default constructor
     */
    public PlayerServerChange() {
    }

    /**
     * Consumes messages on the redis pub/sub interface to determine the online players
     *
     * @param channel The channel that was messaged
     * @param message The content of the message
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.PLAYER_SERVER_CHANGE_CHANNEL)) return;
        //<PLAYER_NAME>|:|<PLAYER_UUID>|:|<OLD_SERVER_NAME>|:|<NEW_SERVER_NAME>
        String[] parts = message.split("\\|:\\|");
        String playerName = parts[0];
        UUID playerUuid = UUID.fromString(parts[1]);
        String oldServerName = parts[2];
        String newServerName = parts[3];
        CytonicServer newServer = Cytosis.getCytonicNetwork().getServers().get(newServerName);
        if (!oldServerName.equals("null")) {
            Cytosis.getCytonicNetwork().getNetoworkPlayersOnServers().remove(playerName);
            return;
        }
        Cytosis.getCytonicNetwork().getNetoworkPlayersOnServers().put(playerName, new PlayerServer(playerName, playerUuid, newServer));
    }
}
