package net.cytonic.cytosis.snooper;

import net.cytonic.protocol.notifyPackets.SnooperNotifyPacket;

@FunctionalInterface
public interface SnooperReceiveEvent {

    void onReceive(SnooperChannel channel, SnooperNotifyPacket.Packet packet);
}
