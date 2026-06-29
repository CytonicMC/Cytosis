package net.cytonic.protocol.impl.notify.parties;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.data.objects.PartyInvite;
import net.cytonic.protocol.impl.notify.parties.PartyInviteNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PartyInviteNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.invites.send.notify";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(PartyInvite invite) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "invite", PartyInvite.CODEC, Packet::invite,
            Packet::new
        );
    }
}
