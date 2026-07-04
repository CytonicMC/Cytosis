package net.cytonic.protocol.impl.notify;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.data.enums.KickReason;
import net.cytonic.protocol.impl.notify.PlayerKickNotifyPacket.Packet;
import net.cytonic.protocol.utils.ProtocolCodecUtils;

public class PlayerKickNotifyPacket extends NoResponse<Packet> {

    @Override
    public String getSubject() {
        return "players.kick";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(UUID uuid, KickReason reason, Component message) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "uuid", Codec.UUID_STRING, Packet::uuid,
            "reason", KickReason.CODEC, Packet::reason,
            "message", ProtocolCodecUtils.COMPONENT, Packet::message,
            Packet::new
        );
    }
}
