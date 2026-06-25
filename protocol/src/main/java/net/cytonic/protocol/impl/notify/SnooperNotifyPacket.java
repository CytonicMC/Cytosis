package net.cytonic.protocol.impl.notify;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.data.objects.StringComponent;
import net.cytonic.protocol.impl.notify.SnooperNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class SnooperNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("SnooperNotifyPacket does not have a default subject!");
    }

    public record Packet(StringComponent message) implements Message<Packet, Void> {

    }
}
