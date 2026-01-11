package net.cytonic.protocol.notifyPackets.parties;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.data.objects.PartyInvite;
import net.cytonic.protocol.notifyPackets.parties.PartyInviteNotifyPacket.Packet;

public class PartyInviteNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.invites.send.notify";
    }

    public record Packet(PartyInvite invite) implements Message<Packet, Void> {

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new PartyInviteNotifyPacket();
        }
    }
}
