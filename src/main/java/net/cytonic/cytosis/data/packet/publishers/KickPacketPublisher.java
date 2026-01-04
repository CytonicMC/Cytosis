package net.cytonic.cytosis.data.packet.publishers;

import java.util.UUID;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.enums.KickReason;
import net.cytonic.cytosis.data.objects.JsonComponent;
import net.cytonic.cytosis.data.packet.packets.PlayerKickPacket;

@CytosisComponent
@NoArgsConstructor
public class KickPacketPublisher {

    /**
     * Sends a message to Nats to kick a player.
     * <p>
     *
     * @param player    The player to kick, on another server
     * @param reason    The reason for kicking the player
     * @param component The kick message displayed
     */
    public void kickPlayer(UUID player, KickReason reason, Component component) {
        new PlayerKickPacket(player, reason, new JsonComponent(component)).publish();
    }
}
