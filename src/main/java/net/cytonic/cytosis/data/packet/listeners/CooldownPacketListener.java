package net.cytonic.cytosis.data.packet.listeners;

import lombok.NoArgsConstructor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.CooldownUpdatePacket;
import net.cytonic.cytosis.data.packet.packets.PacketHandler;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.cytosis.messaging.Subjects;

@CytosisComponent
@NoArgsConstructor
public class CooldownPacketListener {

    @PacketHandler(subject = Subjects.COOLDOWN_UPDATE)
    private void handleCooldownUpdates(CooldownUpdatePacket packet) {
        NetworkCooldownManager cooldownManager = Cytosis.get(NetworkCooldownManager.class);
        switch (packet.getTarget()) {
            case PERSONAL ->
                cooldownManager.setPersonal(packet.getUserUuid(), packet.getNamespace(), packet.getExpiry(), false);
            case GLOBAL -> cooldownManager.setGlobal(packet.getNamespace(), packet.getExpiry(), false);
            default -> throw new IllegalArgumentException("Unsupported target: " + packet.getTarget());
        }
    }
}
