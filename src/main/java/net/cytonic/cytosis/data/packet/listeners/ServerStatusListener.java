package net.cytonic.cytosis.data.packet.listeners;

import lombok.NoArgsConstructor;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.PacketHandler;
import net.cytonic.cytosis.data.packet.packets.ServerStatusPacket;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;

@CytosisComponent
@NoArgsConstructor
public class ServerStatusListener {

    @PacketHandler(subject = Subjects.SERVER_REGISTER)
    private void handleServerRegister(ServerStatusPacket packet) {
        Cytosis.get(CytonicNetwork.class).getServers().put(packet.getId(), packet.server());
        Logger.info("Registered server: " + packet.getId());

        Cytosis.getOnlinePlayers().forEach(player -> {
            if (!player.isAdmin()) {
                return;
            }
            if (player.getPreference(CytosisPreferences.SERVER_ALERTS)) {
                player.sendMessage(
                    Msg.network("Server %s of type %s:%s has been started!",
                        packet.getId(), packet.getGroup(), packet.getType()));
            }
        });
    }

    @PacketHandler(subject = Subjects.SERVER_SHUTDOWN)
    private void handleServerShutdown(ServerStatusPacket packet) {
        Cytosis.get(CytonicNetwork.class).getServers().put(packet.getId(), packet.server());
        Logger.info("Shutdown server: " + packet.getId());

        Cytosis.getOnlinePlayers().forEach(player -> {
            if (!player.isAdmin()) {
                return;
            }
            if (player.getPreference(CytosisPreferences.SERVER_ALERTS)) {
                player.sendMessage(
                    Msg.network("Server %s of type %s:%s has been shut down!",
                        packet.getId(), packet.getGroup(), packet.getType()));
            }
        });
    }

    @PacketHandler(subject = Subjects.SERVER_SHUTDOWN_NOTIFY)
    private void handleServerShutdownNotify(ServerStatusPacket packet) {
        Cytosis.get(CytonicNetwork.class).getServers().remove(packet.getId());
        //todo figure out what this actually does
        //cynder is listening for it, but it never gets published?????
    }

}
