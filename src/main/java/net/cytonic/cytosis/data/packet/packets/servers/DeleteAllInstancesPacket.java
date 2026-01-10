package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class DeleteAllInstancesPacket extends RequestPacket<DeleteAllInstancesPacket, InstanceResponsePacket> {

    private final String instanceType;

    @Override
    protected Serializer<DeleteAllInstancesPacket> getSerializer() {
        return new DefaultGsonSerializer<>(DeleteAllInstancesPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.DELETE_ALL_SERVERS;
    }

    @Override
    protected Class<InstanceResponsePacket> getResponseType() {
        return InstanceResponsePacket.class;
    }
}
