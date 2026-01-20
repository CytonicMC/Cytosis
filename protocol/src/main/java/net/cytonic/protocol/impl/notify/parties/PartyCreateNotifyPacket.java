package net.cytonic.protocol.impl.notify.parties;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.impl.notify.parties.PartyCreateNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PartyCreateNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.create.notify";
    }

    public record Packet(Party party) implements Message<Packet, Void> {

    }
}
