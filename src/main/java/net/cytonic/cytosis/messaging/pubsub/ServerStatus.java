package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.utils.MiniMessageTemplate;
import net.minestom.server.entity.Player;
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
        CytonicNetwork network = Cytosis.getCytonicNetwork();
        if (network == null) return;
        // formatting: <START/STOP>|:|<SERVER_ID>|:|<SERVER_IP>|:|<SERVER_PORT>
        String[] parts = message.split("\\|:\\|");
        if (parts[1].equalsIgnoreCase(Cytosis.SERVER_ID)) return;
        if (parts[0].equalsIgnoreCase("START")) {
            network.getServers().put(parts[1], new CytonicServer(parts[2], parts[1], Integer.parseInt(parts[3])));
            network.getServerAlerts().forEach((uuid, server) -> {
                if (server && Cytosis.getPlayer(uuid).isPresent()) {
                    Player player = Cytosis.getPlayer(uuid).get();
                    if (player.hasPermission("cytosis.commands.serveralerts")) {
                        player.sendMessage(MiniMessageTemplate.MM."<GREEN>A server has started with the id of \{parts[2]}");
                    } else {
                        player.sendMessage(MiniMessageTemplate.MM."<RED>How did you do this");
                        Cytosis.getCytonicNetwork().getServerAlerts().replace(player.getUuid(), false);
                        Cytosis.getDatabaseManager().getMysqlDatabase().setServerAlerts(player.getUuid(), false);
                    }
                }
            });
        } else if (parts[0].equalsIgnoreCase("STOP")) {
            network.getServers().remove(new CytonicServer(parts[2], parts[1], Integer.parseInt(parts[3])));
            network.getServerAlerts().forEach((uuid, server) -> {
                if (server && Cytosis.getPlayer(uuid).isPresent()) {
                    Player player = Cytosis.getPlayer(uuid).get();
                    if (player.hasPermission("cytosis.commands.serveralerts")) {
                        player.sendMessage(MiniMessageTemplate.MM."<GREEN>A server has stoped with the id of \{parts[2]}");
                    } else {
                        player.sendMessage(MiniMessageTemplate.MM."<RED>How did you do this");
                        Cytosis.getCytonicNetwork().getServerAlerts().replace(player.getUuid(), false);
                        Cytosis.getDatabaseManager().getMysqlDatabase().setServerAlerts(player.getUuid(), false);
                    }
                }
            });
        }
    }
}
