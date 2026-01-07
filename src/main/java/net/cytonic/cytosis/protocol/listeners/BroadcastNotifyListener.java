package net.cytonic.cytosis.protocol.listeners;

import com.google.errorprone.annotations.Keep;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.protocol.Notifiable;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.BroadcastProtocolObject;
import net.cytonic.protocol.objects.BroadcastProtocolObject.Packet;

@Keep
public class BroadcastNotifyListener implements Notifiable<Packet> {

    @Override
    public ProtocolObject<Packet, ?> getProtocolObject() {
        return new BroadcastProtocolObject();
    }

    @Override
    public void onMessage(Packet message) {
        Cytosis.getOnlinePlayers().forEach(player -> player.sendMessage(message.message().getComponent()));
    }
}
