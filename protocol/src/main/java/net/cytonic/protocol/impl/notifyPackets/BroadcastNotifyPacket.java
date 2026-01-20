package net.cytonic.protocol.impl.notifyPackets;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.impl.notifyPackets.BroadcastNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class BroadcastNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "chat.broadcast";
    }

    public record Packet(JsonComponent message) implements Message<Packet, Void> {

    }
}
