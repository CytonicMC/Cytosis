package net.cytonic.protocol.impl.objects.parties;

import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyInviteProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.GenericResponse;

public class PartyInviteProtocolObject extends ProtocolObject<Packet, GenericResponse> {

    @Override
    public String getSubject() {
        return "party.invites.send";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<GenericResponse> getReturnCodec() {
        return GenericResponse.CODEC;
    }

    public record Packet(
        @Nullable
        UUID party,
        UUID sender,
        UUID recipient
    ) implements Message<Packet, GenericResponse> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "party_id", Codec.UUID_STRING.optional(), Packet::party,
            "sender_id", Codec.UUID_STRING, Packet::sender,
            "recipient_id", Codec.UUID_STRING, Packet::recipient,
            Packet::new
        );
    }
}
