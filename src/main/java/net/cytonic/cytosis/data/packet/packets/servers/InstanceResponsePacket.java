package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;

@Getter
@AllArgsConstructor
public class InstanceResponsePacket extends Packet<InstanceResponsePacket> {

    private final boolean success;
    private final String message;

    @Override
    protected Serializer<InstanceResponsePacket> getSerializer() {
        return new DefaultGsonSerializer<>(InstanceResponsePacket.class);
    }

//    @Override
//    public String getSubject() {
//        return Subjects.;
//    }
}
