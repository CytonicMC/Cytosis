package net.cytonic.protocol.notifyPackets;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.notifyPackets.SnooperNotifyPacket.Packet;

@Internal
public class SnooperNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("SnooperNotifyPacket does not have a default subject!");
    }

    @Internal
    public record Packet(JsonComponent message) implements Message<Packet, Void> {

    }
}
