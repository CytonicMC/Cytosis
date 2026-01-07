package net.cytonic.cytosis.protocol.listeners;

import com.google.errorprone.annotations.Keep;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.protocol.Notifiable;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.CooldownUpdateProtocolObject;
import net.cytonic.protocol.objects.CooldownUpdateProtocolObject.Packet;

@Keep
public class CooldownNotifyListener implements Notifiable<CooldownUpdateProtocolObject.Packet> {

    @Override
    public ProtocolObject<Packet, ?> getProtocolObject() {
        return new CooldownUpdateProtocolObject();
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
