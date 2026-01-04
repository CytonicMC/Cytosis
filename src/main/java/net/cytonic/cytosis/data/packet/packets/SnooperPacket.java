package net.cytonic.cytosis.data.packet.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.objects.JsonComponent;
import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class SnooperPacket extends Packet<SnooperPacket> {

    private final JsonComponent message;
    private final transient String subject;

    @Override
    protected Serializer<SnooperPacket> getSerializer() {
        return new DefaultGsonSerializer<>(SnooperPacket.class);
    }
}
