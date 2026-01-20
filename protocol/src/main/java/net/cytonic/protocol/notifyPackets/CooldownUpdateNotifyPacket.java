package net.cytonic.protocol.notifyPackets;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.notifyPackets.CooldownUpdateNotifyPacket.Packet;

@Internal
public class CooldownUpdateNotifyPacket extends NoResponse<Packet> {

    @Override
    public String getSubject() {
        return "cooldown.update";
    }

    public enum Type {
        GLOBAL,
        PERSONAL
    }

    @Internal
    public record Packet(Key namespace, Instant expiry, UUID userUUID, Type type) implements Message<Packet, Void> {

    }
}
