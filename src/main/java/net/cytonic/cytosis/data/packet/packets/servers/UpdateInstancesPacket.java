package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class UpdateInstancesPacket extends Packet<UpdateInstancesPacket> {

    private final String instanceType;

    @Override
    protected Serializer<UpdateInstancesPacket> getSerializer() {
        return new DefaultGsonSerializer<>(UpdateInstancesPacket.class);
    }
}
