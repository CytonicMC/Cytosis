package net.cytonic.protocol.impl.notify;

import java.time.Instant;
import java.util.UUID;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.impl.notify.CooldownUpdateNotifyPacket.Packet;
import net.cytonic.protocol.utils.ProtocolCodecUtils;

public class CooldownUpdateNotifyPacket extends NoResponse<Packet> {

    @Override
    public String getSubject() {
        return "cooldown.update";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public enum Type {
        GLOBAL,
        PERSONAL;
        public static final Codec<Type> CODEC = Codec.Enum(Type.class);
    }

    public record Packet(
        Key namespace,
        @Nullable Instant expiry,
        @Nullable UUID player,
        Type type
    ) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "namespace", Codec.KEY, Packet::namespace,
            "expiry", ProtocolCodecUtils.INSTANT.optional(), Packet::expiry,
            "player", Codec.UUID_STRING.optional(), Packet::player,
            "type", Type.CODEC, Packet::type,
            Packet::new
        );
    }
}
