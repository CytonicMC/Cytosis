package net.cytonic.cytosis.data.packet.packets;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.key.Key;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.IllegalSubjectException;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;

/**
 * A class that represents a packet for updating cooldowns
 */
@Getter
@AllArgsConstructor
public class CooldownUpdatePacket extends Packet<CooldownUpdatePacket> {


    private final Key namespace;
    private final Instant expiry;
    private final UUID userUuid;

    @Override
    protected Serializer<CooldownUpdatePacket> getSerializer() {
        return new DefaultGsonSerializer<>(CooldownUpdatePacket.class);
    }

    @Override
    public String getSubject() {
        throw new IllegalSubjectException();
    }
}
