package net.cytonic.protocol.notifyPackets;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.notifyPackets.BroadcastNotifyPacket.Packet;

public class BroadcastNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "chat.broadcast";
    }

    public record Packet(JsonComponent message) implements Message<Packet, Void> {

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new BroadcastNotifyPacket();
        }
    }
}
