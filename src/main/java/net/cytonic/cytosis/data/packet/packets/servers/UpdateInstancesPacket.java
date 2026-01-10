package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class UpdateInstancesPacket extends RequestPacket<UpdateInstancesPacket, InstanceResponsePacket> {

    private final String instanceType;

    @Override
    protected Serializer<UpdateInstancesPacket> getSerializer() {
        return new DefaultGsonSerializer<>(UpdateInstancesPacket.class);
    }

    @Override
    protected Class<InstanceResponsePacket> getResponseType() {
        return InstanceResponsePacket.class;
    }
}
