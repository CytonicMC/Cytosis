package net.cytonic.cytosis.messaging.pubsub;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.obj.CytonicServer;
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
     * @param message The connent of the message
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(RedisDatabase.SERVER_STATUS_CHANNEL)) return;
        CytonicNetwork network = Cytosis.getCytonicNetwork();
        if (network == null) return;
        // formatting: <START/STOP>|:|<SERVER_ID>|:|<SERVER_IP>|:|<SERVER_PORT>
        String[] parts = message.split("\\|:\\|");
        if (parts[0].equalsIgnoreCase("START")) {
            network.getServers().add(new CytonicServer(parts[2], parts[1], Integer.parseInt(parts[3])));
        } else if (parts[0].equalsIgnoreCase("STOP")) {
            network.getServers().remove(new CytonicServer(parts[2], parts[1], Integer.parseInt(parts[3])));
        }
    }
}
