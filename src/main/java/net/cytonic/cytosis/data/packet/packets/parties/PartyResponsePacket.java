package net.cytonic.cytosis.data.packet.packets.parties;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class PartyResponsePacket extends Packet<PartyResponsePacket> {

    private final boolean success;
    private final String message;

    @Override
    protected Serializer<PartyResponsePacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyResponsePacket.class);
    }
}
