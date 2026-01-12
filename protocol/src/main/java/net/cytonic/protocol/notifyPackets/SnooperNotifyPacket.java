package net.cytonic.protocol.notifyPackets;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.notifyPackets.SnooperNotifyPacket.Packet;

public class SnooperNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new RuntimeException("oh noes");
    }

    public record Packet(JsonComponent message) implements Message<Packet, Void> {

    }
}
