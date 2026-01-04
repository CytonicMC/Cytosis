package net.cytonic.cytosis.data.packet.packets.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.packets.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.packets.Packet;
import net.cytonic.cytosis.data.packet.packets.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class DeleteInstancePacket extends Packet<DeleteInstancePacket> {

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
}
