package net.cytonic.protocol.impl.objects.parties;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyStateProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.GenericResponse;

@NoArgsConstructor
@AllArgsConstructor
public class PartyStateProtocolObject extends ProtocolObject<Packet, GenericResponse> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
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
        UUID party,
        UUID player,
        boolean state
    ) implements Message<Packet, GenericResponse> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "party_id", Codec.UUID_STRING, Packet::party,
            "player_id", Codec.UUID_STRING, Packet::player,
            "state", Codec.BOOLEAN, Packet::state,
            Packet::new
        );
    }
}
