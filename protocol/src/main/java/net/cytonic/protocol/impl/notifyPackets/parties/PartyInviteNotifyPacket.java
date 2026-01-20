package net.cytonic.protocol.impl.notifyPackets.parties;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.data.objects.PartyInvite;
import net.cytonic.protocol.impl.notifyPackets.parties.PartyInviteNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PartyInviteNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.invites.send.notify";
    }

    public record Packet(PartyInvite invite) implements Message<Packet, Void> {

    }
}
