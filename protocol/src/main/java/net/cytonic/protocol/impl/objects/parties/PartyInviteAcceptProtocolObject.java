package net.cytonic.protocol.impl.objects.parties;

import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyInviteAcceptProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.GenericResponse;

public class PartyInviteAcceptProtocolObject extends ProtocolObject<Packet, GenericResponse> {

    @Override
    public String getSubject() {
        return "party.invites.accept";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<GenericResponse> getReturnCodec() {
        return GenericResponse.CODEC;
    }

    public record Packet(UUID request) implements Message<Packet, GenericResponse> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "request_id", Codec.UUID_STRING, Packet::request,
            Packet::new
        );
    }
}
