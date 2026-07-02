package net.cytonic.protocol.impl.notify;

import java.time.Instant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notify.ServerStatusNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;
import net.cytonic.protocol.utils.ProtocolCodecUtils;

public class ServerStatusNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("ServerStatusNotifyPacket does not have a default subject!");
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(
        String ip,
        String id,
        int port,
        @Nullable
        Instant lastSeen,
        Key type
    ) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "ip", Codec.STRING, Packet::ip,
            "id", Codec.STRING, Packet::id,
            "port", Codec.INT, Packet::port,
            "last_seen", ProtocolCodecUtils.INSTANT.optional(), Packet::lastSeen,
            "type", Codec.KEY, Packet::type,
            Packet::new
        );
    }
}
