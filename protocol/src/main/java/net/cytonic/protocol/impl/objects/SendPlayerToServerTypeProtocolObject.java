package net.cytonic.protocol.impl.objects;

import java.util.UUID;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.SendPlayerToServerTypeProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.GenericResponse;

public class SendPlayerToServerTypeProtocolObject extends ProtocolObject<Packet, GenericResponse> {

    @Override
    public String getSubject() {
        return "players.send.generic";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<GenericResponse> getReturnCodec() {
        return GenericResponse.CODEC;
    }

    public record Packet(UUID player, Key type) implements Message<Packet, GenericResponse> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "player", Codec.UUID_STRING, Packet::player,
            "type", Codec.KEY, Packet::type,
            Packet::new
        );
    }
}
