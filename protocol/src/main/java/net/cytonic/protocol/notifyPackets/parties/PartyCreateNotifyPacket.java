package net.cytonic.protocol.notifyPackets.parties;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.notifyPackets.parties.PartyCreateNotifyPacket.Packet;

public class PartyCreateNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.create.notify";
    }

    public record Packet(Party party) implements Message<Packet, Void> {

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new PartyCreateNotifyPacket();
        }
    }
}
