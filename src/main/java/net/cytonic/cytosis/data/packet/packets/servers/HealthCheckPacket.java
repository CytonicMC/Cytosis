package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;

@Getter
@AllArgsConstructor
public class HealthCheckPacket extends Packet<HealthCheckPacket> {

    private transient final String subject;

    @Override
    protected Serializer<HealthCheckPacket> getSerializer() {
        return new DefaultGsonSerializer<>(HealthCheckPacket.class);
    }
}
