package net.cytonic.cytosis.data.packet.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.objects.JsonComponent;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class BroadcastPacket extends Packet<BroadcastPacket> {

    private final JsonComponent message;

    @Override
    public String getSubject() {
        return Subjects.CHAT_BROADCAST;
    }

    @Override
    protected Serializer<BroadcastPacket> getSerializer() {
        return new DefaultGsonSerializer<>(BroadcastPacket.class);
    }
}
