package net.cytonic.protocol.notifyPackets;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.notifyPackets.BroadcastNotifyPacket.Packet;

@Internal
public class BroadcastNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "chat.broadcast";
    }

    @Internal
    public record Packet(JsonComponent message) implements Message<Packet, Void> {

    }
}
