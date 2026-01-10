package net.cytonic.cytosis.data.packet.packets.servers;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class SendPlayerToServerPacket extends RequestPacket<SendPlayerToServerPacket, ServerSendReponsePacket> {

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

    @Override
    protected Class<ServerSendReponsePacket> getResponseType() {
        return ServerSendReponsePacket.class;
    }
}
