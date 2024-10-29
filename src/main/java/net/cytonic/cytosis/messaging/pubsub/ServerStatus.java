package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.containers.ServerStatusContainer;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.objects.CytonicServer;
import net.cytonic.utils.MiniMessageTemplate;
import redis.clients.jedis.JedisPubSub;

/**
 * A pub sub that handles server starts and stops
 */
public class ServerStatus extends JedisPubSub {


    /**
     * Default constructor
     */
    public ServerStatus() {
    }

    /**
     * Consumes messages on the redis pub/sub interface to determine the online players
     *
     * @param channel The channel that was messaged
     * @param message The content of the message
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.SERVER_STATUS_CHANNEL)) return;
        if (Cytosis.getCytonicNetwork() == null) return;

        ServerStatusContainer container = ServerStatusContainer.deserialize(message);
        if (container.serverName().equalsIgnoreCase(Cytosis.SERVER_ID)) return;

        CytonicServer server = container.server();
        PreferenceManager manager = Cytosis.getPreferenceManager();
        if (container.mode() == ServerStatusContainer.Mode.START) {
            Cytosis.getCytonicNetwork().getServers().put(container.serverName(), server);
            Cytosis.getOnlinePlayers().forEach((player) -> {
                if (manager.getPlayerPreference(player.getUuid(), CytosisPreferences.SERVER_ALERTS) && !player.hasPermission("cytosis.commands.serveralerts")) {
                    Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.SERVER_ALERTS, false);
                    player.sendMessage(MiniMessageTemplate.MM."<RED>How did you do this");
                    return;
                }
                if (manager.getPlayerPreference(player.getUuid(), CytosisPreferences.SERVER_ALERTS) && player.hasPermission("cytosis.commands.serveralerts"))
                    player.sendMessage(MiniMessageTemplate.MM."<GREEN>A server has started with the id of \{server.id()}");
            });
        } else if (container.mode() == ServerStatusContainer.Mode.STOP) {
            Cytosis.getCytonicNetwork().getServers().remove(server);
            Cytosis.getOnlinePlayers().forEach((player) -> {
                if (manager.getPlayerPreference(player.getUuid(), CytosisPreferences.SERVER_ALERTS) && !player.hasPermission("cytosis.commands.serveralerts")) {
                    manager.updatePlayerPreference(player.getUuid(), CytosisNamespaces.SERVER_ALERTS, false);
                    player.sendMessage(MiniMessageTemplate.MM."<RED>How did you do this");
                    return;
                }
                if (manager.getPlayerPreference(player.getUuid(), CytosisPreferences.SERVER_ALERTS) && player.hasPermission("cytosis.commands.serveralerts"))
                    player.sendMessage(MiniMessageTemplate.MM."<RED>A server has stopped with the id of \{server.id()}");
            });
        }
    }
}
