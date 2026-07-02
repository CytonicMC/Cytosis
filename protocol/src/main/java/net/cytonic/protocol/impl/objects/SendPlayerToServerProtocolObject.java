package net.cytonic.protocol.impl.objects;

import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.SendPlayerToServerProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.GenericResponse;

public class SendPlayerToServerProtocolObject extends ProtocolObject<Packet, GenericResponse> {

    @Override
    public String getSubject() {
        return "players.send";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<GenericResponse> getReturnCodec() {
        return GenericResponse.CODEC;
    }

    public record Packet(UUID player, String serverId) implements Message<Packet, GenericResponse> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "player", Codec.UUID_STRING, Packet::player,
            "serverId", Codec.STRING, Packet::serverId,
            Packet::new
        );
    }
}
