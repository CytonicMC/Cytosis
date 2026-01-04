package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;

@Getter
@AllArgsConstructor
public class UpdateInstancesPacket extends Packet<UpdateInstancesPacket> {

    private final String instanceType;

    @Override
    protected Serializer<UpdateInstancesPacket> getSerializer() {
        return new DefaultGsonSerializer<>(UpdateInstancesPacket.class);
    }
}
