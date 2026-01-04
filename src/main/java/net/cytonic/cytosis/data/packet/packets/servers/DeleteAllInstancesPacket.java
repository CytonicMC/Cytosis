package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class DeleteAllInstancesPacket extends Packet<DeleteAllInstancesPacket> {

    private final String instanceType;

    @Override
    protected Serializer<DeleteAllInstancesPacket> getSerializer() {
        return new DefaultGsonSerializer<>(DeleteAllInstancesPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.DELETE_ALL_SERVERS;
    }
}
