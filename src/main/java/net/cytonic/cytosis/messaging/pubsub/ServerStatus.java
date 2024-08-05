package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.objects.CytonicServer;
import net.cytonic.utils.MiniMessageTemplate;
import net.minestom.server.utils.NamespaceID;
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
        // formatting: <START/STOP>|:|<SERVER_ID>|:|<SERVER_IP>|:|<SERVER_PORT>
        String[] parts = message.split("\\|:\\|");
        if (parts[1].equalsIgnoreCase(Cytosis.SERVER_ID)) return;
        CytonicServer server = new CytonicServer(parts[2], parts[1], Integer.parseInt(parts[3]));
        PreferenceManager manager = Cytosis.getPreferenceManager();
        if (parts[0].equalsIgnoreCase("START")) {
            Cytosis.getCytonicNetwork().getServers().put(parts[1], server);
            Cytosis.getOnlinePlayers().forEach((player) -> {
                if (Boolean.parseBoolean(manager.getPlayerPreference(player.getUuid(), NamespaceID.from("cytosis:server_alerts"))) && !player.hasPermission("cytosis.commands.serveralerts")) {
                    Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), NamespaceID.from("cytosis:server_alerts"), false);
                    player.sendMessage(MiniMessageTemplate.MM."<RED>How did you do this");
                    return;
                }
                if (Boolean.parseBoolean(manager.getPlayerPreference(player.getUuid(), NamespaceID.from("cytosis:server_alerts"))) && player.hasPermission("cytosis.commands.serveralerts"))
                    player.sendMessage(MiniMessageTemplate.MM."<GREEN>A server has started with the id of \{parts[1]}");
            });
        } else if (parts[0].equalsIgnoreCase("STOP")) {
            Cytosis.getCytonicNetwork().getServers().remove(server);
            Cytosis.getOnlinePlayers().forEach((player) -> {
                if (Boolean.parseBoolean(manager.getPlayerPreference(player.getUuid(), NamespaceID.from("cytosis:server_alerts"))) && !player.hasPermission("cytosis.commands.serveralerts")) {
                    Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), NamespaceID.from("cytosis:server_alerts"), false);
                    player.sendMessage(MiniMessageTemplate.MM."<RED>How did you do this");
                    return;
                }
                if (Boolean.parseBoolean(manager.getPlayerPreference(player.getUuid(), NamespaceID.from("cytosis:server_alerts"))) && player.hasPermission("cytosis.commands.serveralerts"))
                    player.sendMessage(MiniMessageTemplate.MM."<GREEN>A server has stopped with the id of \{parts[1]}");
            });
        }
    }
}
