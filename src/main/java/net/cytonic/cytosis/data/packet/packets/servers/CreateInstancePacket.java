package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class CreateInstancePacket extends Packet<CreateInstancePacket> {

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
}
