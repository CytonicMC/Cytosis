package net.cytonic.cytosis.data.packet.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.objects.JsonComponent;

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
