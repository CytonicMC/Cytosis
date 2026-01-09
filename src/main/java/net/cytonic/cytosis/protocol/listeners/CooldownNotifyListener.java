package net.cytonic.cytosis.protocol.listeners;

import com.google.errorprone.annotations.Keep;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.protocol.NotifyListener;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.CooldownUpdateNotifyPacket;
import net.cytonic.protocol.notifyPackets.CooldownUpdateNotifyPacket.Packet;

@Keep
public class CooldownNotifyListener implements NotifyListener<Packet> {

    @Override
    public ProtocolObject<Packet, ?> getProtocolObject() {
        return new CooldownUpdateNotifyPacket();
    }

    @Override
    public void onMessage(Packet message) {
        NetworkCooldownManager cooldownManager = Cytosis.get(NetworkCooldownManager.class);
        switch (message.type()) {
            case PERSONAL ->
                cooldownManager.setPersonal(message.userUUID(), message.namespace(), message.expiry(), false);
            case GLOBAL -> cooldownManager.setGlobal(message.namespace(), message.expiry(), false);
            default -> throw new IllegalArgumentException("Unsupported target: " + message.type());
        }
    }
}
