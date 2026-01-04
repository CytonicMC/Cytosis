package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;

@Getter
@AllArgsConstructor
public class ServerSendReponsePacket extends Packet<ServerSendReponsePacket> {

    private final boolean success;
    private final String message;

    @Override
    protected Serializer<ServerSendReponsePacket> getSerializer() {
        return new DefaultGsonSerializer<>(ServerSendReponsePacket.class);
    }
}
