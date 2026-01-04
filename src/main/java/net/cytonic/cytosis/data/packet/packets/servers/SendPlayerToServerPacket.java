package net.cytonic.cytosis.data.packet.packets.servers;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class SendPlayerToServerPacket extends Packet<SendPlayerToServerPacket> {

    public final UUID player;
    private final String serverId;

    @Override
    protected Serializer<SendPlayerToServerPacket> getSerializer() {
        return new DefaultGsonSerializer<>(SendPlayerToServerPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PLAYER_SEND;
    }
}
