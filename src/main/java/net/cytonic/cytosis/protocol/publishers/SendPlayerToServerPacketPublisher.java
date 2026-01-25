package net.cytonic.cytosis.protocol.publishers;

import java.util.UUID;

import lombok.NoArgsConstructor;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.impl.objects.SendPlayerToServerProtocolObject;
import net.cytonic.protocol.impl.objects.SendPlayerToServerTypeProtocolObject;

@CytosisComponent
@NoArgsConstructor
public class SendPlayerToServerPacketPublisher {

    /**
     * Sends a message to the NATS server telling the proxies to move a player to a different server
     *
     * @param uuid   The player to move
     * @param server the destination server
     */
    public void sendPlayerToServer(UUID uuid, CytonicServer server) {
        new SendPlayerToServerProtocolObject.Packet(uuid, server.id()).request((response, throwable) -> {
            if (Cytosis.getPlayer(uuid).isEmpty()) {
                return;
            }
            Player player = Cytosis.getPlayer(uuid).get();
            if (throwable != null) {
                player.sendMessage(Msg.serverError("An error occurred whilst sending you to %s!", server.id()));
            }

            if (!response.success()) {
                player.sendMessage(
                    Msg.serverError("An error occurred whilst sending you to %s! <red>(%s)</red>", server.id(),
                        response.message()));
            } else {
                player.sendMessage(Msg.network("Sending you to %s!", server.id()));
            }
        });
    }

    public void sendPlayerToGenericServer(UUID player, String group, String id, @Nullable String displayName) {
        new SendPlayerToServerTypeProtocolObject.Packet(player, group, id).request((response, throwable) -> {
            if (Cytosis.getPlayer(player).isEmpty()) {
                return;
            }
            Player p = Cytosis.getPlayer(player).get();
            if (throwable != null) {
                p.sendMessage(Msg.serverError("An error occurred whilst sending you to %s!",
                    displayName == null ? "a server" : displayName));
                Logger.error("An error occurred whilst sending " + player + " to a generic " + group + ":" + id
                    + "! <red>(%s)</red>", throwable);
            }

            if (!response.success()) {
                p.sendMessage(Msg.serverError("An error occurred whilst sending you to %s! <red>(%s)</red>",
                    displayName == null ? "a server" : displayName, response.message()));
            } else {
                p.sendMessage(Msg.network("Sending you to %s!", displayName == null ? "a server" : displayName));
            }
        });
    }
}
