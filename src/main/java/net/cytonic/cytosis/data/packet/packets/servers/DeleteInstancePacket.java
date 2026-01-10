package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class DeleteInstancePacket extends RequestPacket<DeleteInstancePacket, InstanceResponsePacket> {

    private final String instanceType;
    private final String allocId;

    @Override
    protected Serializer<DeleteInstancePacket> getSerializer() {
        return new DefaultGsonSerializer<>(DeleteInstancePacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.DELETE_SERVER;
    }

    @Override
    protected Class<InstanceResponsePacket> getResponseType() {
        return InstanceResponsePacket.class;
    }
}
