package net.cytonic.cytosis.protocol.listeners;

import com.google.errorprone.annotations.Keep;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.impl.notify.BroadcastNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyListener;

@Keep
public class BroadcastNotifyListener implements NotifyListener<Packet> {

    @Override
    public void onMessage(Packet message, NotifyData notifyData) {
        Cytosis.getOnlinePlayers().forEach(player -> player.sendMessage(message.message().getComponent()));
    }
}
