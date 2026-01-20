package net.cytonic.protocol.impl.notifyPackets;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.key.Key;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.impl.notifyPackets.CooldownUpdateNotifyPacket.Packet;

public class CooldownUpdateNotifyPacket extends NoResponse<Packet> {

    @Override
    public String getSubject() {
        return "cooldown.update";
    }

    public enum Type {
        GLOBAL,
        PERSONAL
    }

    public record Packet(Key namespace, Instant expiry, UUID userUUID, Type type) implements Message<Packet, Void> {

    }
}
