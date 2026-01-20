package net.cytonic.protocol.notifyPackets.parties;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.data.objects.PartyInvite;
import net.cytonic.protocol.notifyPackets.parties.PartyInviteNotifyPacket.Packet;

@Internal
public class PartyInviteNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.invites.send.notify";
    }

    @Internal
    public record Packet(PartyInvite invite) implements Message<Packet, Void> {

    }
}
