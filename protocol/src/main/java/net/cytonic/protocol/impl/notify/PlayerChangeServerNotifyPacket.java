package net.cytonic.protocol.impl.notify;

import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notify.PlayerChangeServerNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PlayerChangeServerNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "players.server_change.notify";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(
        UUID player,
        String oldServer,
        String newServer
    ) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "player", Codec.UUID_STRING, Packet::player,
            "oldServer", Codec.STRING, Packet::oldServer,
            "newServer", Codec.STRING, Packet::newServer,
            Packet::new
        );
    }
}
