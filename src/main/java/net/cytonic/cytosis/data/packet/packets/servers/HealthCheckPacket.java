package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.IllegalSubjectException;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class HealthCheckPacket extends Packet<HealthCheckPacket> {

    @Override
    public String getSubject() {
        throw new IllegalSubjectException();
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }

    @Override
    protected Serializer<HealthCheckPacket> getSerializer() {
        return new DefaultGsonSerializer<>(HealthCheckPacket.class);
    }
}
