package net.cytonic.protocol.impl.notifyPackets;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.impl.notifyPackets.SnooperNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class SnooperNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("SnooperNotifyPacket does not have a default subject!");
    }

    public record Packet(JsonComponent message) implements Message<Packet, Void> {

    }
}
