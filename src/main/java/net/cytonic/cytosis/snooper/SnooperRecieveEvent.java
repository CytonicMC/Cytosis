package net.cytonic.cytosis.snooper;

import net.cytonic.cytosis.data.packet.packets.SnooperPacket;

@FunctionalInterface
public interface SnooperRecieveEvent {

    void onReceive(SnooperChannel channel, SnooperPacket packet);
}
