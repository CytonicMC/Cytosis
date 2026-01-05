package net.cytonic.cytosis.data.packet.listeners;

import lombok.NoArgsConstructor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.packet.packets.CooldownUpdatePacket;
import net.cytonic.cytosis.data.packet.utils.PacketData;
import net.cytonic.cytosis.data.packet.utils.PacketHandler;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.cytosis.messaging.Subjects;

@CytosisComponent
@NoArgsConstructor
public class CooldownPacketListener {

    private final NetworkCooldownManager cooldownManager = Cytosis.get(NetworkCooldownManager.class);

    @PacketHandler(subject = Subjects.COOLDOWN_UPDATE_NOTIFY)
    private void handleCooldownUpdates(CooldownUpdatePacket packet, PacketData data) {
        String subj = data.subject().split("\\.")[2];
        switch (subj) {
            case "personal" ->
                cooldownManager.setPersonal(packet.getUserUuid(), packet.getNamespace(), packet.getExpiry(), false);
            case "global" -> cooldownManager.setGlobal(packet.getNamespace(), packet.getExpiry(), false);
            default -> throw new IllegalArgumentException("Unsupported target: " + subj);
        }
    }
}
