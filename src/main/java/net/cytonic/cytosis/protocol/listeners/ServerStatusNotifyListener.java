package net.cytonic.cytosis.protocol.listeners;

import com.google.errorprone.annotations.Keep;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.NotifyListener;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket.Packet;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket.Type;

public class ServerStatusNotifyListener {

    public static CytonicServer getServer(Packet packet) {
        return new CytonicServer(
            packet.ip(),
            packet.id(),
            packet.port(),
            packet.type(),
            packet.group()
        );
    }

    @Keep
    public static class ServerStatusStartupNotifyListener implements NotifyListener<Packet> {

        @Override
        public ProtocolObject<Packet, ?> getProtocolObject() {
            return new ServerStatusNotifyPacket(Type.STARTUP);
        }

        @Override
        public void onMessage(Packet message) {
            CytonicServer server = getServer(message);
            Cytosis.get(CytonicNetwork.class).getServers().put(message.id(), server);
            Logger.info("Registered server: " + message.id());

            Cytosis.getOnlinePlayers().forEach(player -> {
                if (!player.isAdmin()) {
                    return;
                }
                if (player.getPreference(CytosisPreferences.SERVER_ALERTS)) {
                    player.sendMessage(
                        Msg.network("Server %s of type %s:%s has been started!",
                            message.id(), server.group(), server.type()));
                }
            });
        }
    }

    @Keep
    public static class ServerStatusShutdownNotifyListener implements NotifyListener<Packet> {

        @Override
        public ProtocolObject<Packet, ?> getProtocolObject() {
            return new ServerStatusNotifyPacket(Type.SHUTDOWN);
        }

        @Override
        public void onMessage(Packet message) {
            CytonicServer server = getServer(message);
            Cytosis.get(CytonicNetwork.class).getServers().put(message.id(), server);
            Logger.info("Shutdown server: " + message.id());

            Cytosis.getOnlinePlayers().forEach(player -> {
                if (!player.isAdmin()) {
                    return;
                }
                if (player.getPreference(CytosisPreferences.SERVER_ALERTS)) {
                    player.sendMessage(
                        Msg.network("Server %s of type %s:%s has been shut down!",
                            message.id(), server.group(), server.type()));
                }
            });
        }
    }

    @Keep
    public static class ServerStatusNotifyShutdownNotifyListener implements NotifyListener<Packet> {

        @Override
        public ProtocolObject<Packet, ?> getProtocolObject() {
            return new ServerStatusNotifyPacket(Type.NOTIFY_SHUTDOWN);
        }

        @Override
        public void onMessage(Packet message) {
            Cytosis.get(CytonicNetwork.class).getServers().remove(message.id());
        }
    }
}
