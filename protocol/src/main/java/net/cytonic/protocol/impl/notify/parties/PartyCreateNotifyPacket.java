package net.cytonic.protocol.impl.notify.parties;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.impl.notify.parties.PartyCreateNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PartyCreateNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.create.notify";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(Party party) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "party", Party.CODEC, Packet::party,
            Packet::new
        );
    }
}
