package net.cytonic.protocol.notifyPackets.parties;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NotifyPacket;
import net.cytonic.protocol.data.objects.Party;
import net.cytonic.protocol.notifyPackets.parties.PartyCreateNotifyPacket.Packet;

@Internal
public class PartyCreateNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.create.notify";
    }

    @Internal
    public record Packet(Party party) implements Message<Packet, Void> {

    }
}
