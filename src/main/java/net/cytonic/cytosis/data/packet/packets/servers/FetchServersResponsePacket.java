package net.cytonic.cytosis.data.packet.packets.servers;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket;

@Getter
@AllArgsConstructor
public class FetchServersResponsePacket extends Packet<FetchServersResponsePacket> {

    private final List<ServerStatusNotifyPacket.Packet> servers;

    @Override
    protected Serializer<FetchServersResponsePacket> getSerializer() {
        return new DefaultGsonSerializer<>(FetchServersResponsePacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.SERVER_LIST;
    }
}
