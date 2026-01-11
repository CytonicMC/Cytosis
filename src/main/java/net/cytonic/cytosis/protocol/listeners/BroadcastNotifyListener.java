package net.cytonic.cytosis.protocol.listeners;

import com.google.errorprone.annotations.Keep;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.NotifyListener;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.BroadcastNotifyPacket;
import net.cytonic.protocol.notifyPackets.BroadcastNotifyPacket.Packet;

@Keep
public class BroadcastNotifyListener implements NotifyListener<Packet> {

    @Override
    public ProtocolObject<Packet, ?> getProtocolObject() {
        return new BroadcastNotifyPacket();
    }

    @Override
    public void onMessage(Packet message, NotifyData notifyData) {
        Cytosis.getOnlinePlayers().forEach(player -> player.sendMessage(message.message().getComponent()));
    }
}
