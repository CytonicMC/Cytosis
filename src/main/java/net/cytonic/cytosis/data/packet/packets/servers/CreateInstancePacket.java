package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class CreateInstancePacket extends RequestPacket<CreateInstancePacket, InstanceResponsePacket> {

    private final String instanceType;
    private final int quantity;

    @Override
    protected Serializer<CreateInstancePacket> getSerializer() {
        return new DefaultGsonSerializer<>(CreateInstancePacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.CREATE_SERVER;
    }

    @Override
    protected Class<InstanceResponsePacket> getResponseType() {
        return InstanceResponsePacket.class;
    }
}
