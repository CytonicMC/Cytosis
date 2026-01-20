package net.cytonic.cytosis.protocol.listeners;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.impl.notify.ServerStatusNotifyPacket;
import net.cytonic.protocol.utils.NotifyHandler;

public class ServerStatusNotifyListener {

    @NotifyHandler(subject = "servers.register")
    public static void serverStartup(ServerStatusNotifyPacket.Packet packet) {
        CytonicServer server = new CytonicServer(packet);
        Cytosis.get(CytonicNetwork.class).getServers().put(packet.id(), server);
        Logger.info("Registered server: " + packet.id());

        Cytosis.getOnlinePlayers().forEach(player -> {
            if (!player.isAdmin()) {
                return;
            }
            if (player.getPreference(CytosisPreferences.SERVER_ALERTS)) {
                player.sendMessage(
                    Msg.network("Server %s of type %s:%s has been started!",
                        packet.id(), server.group(), server.type()));
            }
        });
    }

    @NotifyHandler(subject = "servers.shutdown")
    public static void serverShutdown(ServerStatusNotifyPacket.Packet packet) {
        CytonicServer server = new CytonicServer(packet);
        Cytosis.get(CytonicNetwork.class).getServers().put(packet.id(), server);
        Logger.info("Shutdown server: " + packet.id());

        Cytosis.getOnlinePlayers().forEach(player -> {
            if (!player.isAdmin()) {
                return;
            }
            if (player.getPreference(CytosisPreferences.SERVER_ALERTS)) {
                player.sendMessage(
                    Msg.network("Server %s of type %s:%s has been shut down!",
                        packet.id(), server.group(), server.type()));
            }
        });
    }

    @NotifyHandler(subject = "servers.proxy.shutdown.notify")
    public static void serverProxyShutdown(ServerStatusNotifyPacket.Packet packet) {
        Cytosis.get(CytonicNetwork.class).getServers().remove(packet.id());
    }
}
