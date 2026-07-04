package net.cytonic.protocol.impl.notify;

import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notify.PlayerRankUpdateNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PlayerRankUpdateNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "players.rank.update";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(UUID player, String rank) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "player", Codec.UUID_STRING, Packet::player,
            "rank", Codec.STRING, Packet::rank,
            Packet::new
        );
    }
}
