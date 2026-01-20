package net.cytonic.protocol.impl.notify;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.impl.notify.BroadcastNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class BroadcastNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "chat.broadcast";
    }

    public record Packet(JsonComponent message) implements Message<Packet, Void> {

    }
}
