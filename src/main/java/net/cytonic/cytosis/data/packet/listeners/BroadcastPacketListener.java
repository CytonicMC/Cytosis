package net.cytonic.cytosis.data.packet.listeners;

import lombok.NoArgsConstructor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.BroadcastPacket;
import net.cytonic.cytosis.data.packet.packets.PacketHandler;
import net.cytonic.cytosis.messaging.Subjects;

@CytosisComponent
@NoArgsConstructor
public class BroadcastPacketListener {

    @PacketHandler(subject = Subjects.CHAT_BROADCAST)
    private void handleBroadcast(BroadcastPacket packet) {
        Cytosis.getOnlinePlayers().forEach(player -> player.sendMessage(packet.getMessage().getComponent()));
    }
}
