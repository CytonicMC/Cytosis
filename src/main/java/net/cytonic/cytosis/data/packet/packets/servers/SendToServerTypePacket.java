package net.cytonic.cytosis.data.packet.packets.servers;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class SendToServerTypePacket extends Packet<SendToServerTypePacket> {

    private final UUID player;
    private final String group;
    private final String type;

    @Override
    protected Serializer<SendToServerTypePacket> getSerializer() {
        return new DefaultGsonSerializer<>(SendToServerTypePacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PLAYER_SEND_GENERIC;
    }
}
