package net.cytonic.protocol.impl.objects.parties;

import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyLeaveProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.GenericResponse;

public class PartyLeaveProtocolObject extends ProtocolObject<Packet, GenericResponse> {

    @Override
    public String getSubject() {
        return "party.leave.request";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<GenericResponse> getReturnCodec() {
        return GenericResponse.CODEC;
    }

    public record Packet(UUID player) implements Message<Packet, GenericResponse> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "player_id", Codec.UUID_STRING, Packet::player,
            Packet::new
        );
    }
}
