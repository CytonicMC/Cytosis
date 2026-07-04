package net.cytonic.protocol.impl.notify.parties;

import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notify.parties.PartyInviteExpireNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PartyInviteExpireNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.invites.expire";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(
        UUID request,
        UUID party,
        UUID recipient,
        UUID sender
    ) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "request_id", Codec.UUID_STRING, Packet::request,
            "party_id", Codec.UUID_STRING, Packet::party,
            "recipient", Codec.UUID_STRING, Packet::recipient,
            "sender_id", Codec.UUID_STRING, Packet::sender,
            Packet::new
        );
    }
}
