package net.cytonic.protocol.impl.notifyPackets.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.impl.notifyPackets.parties.PartyInviteExpireNotifyPacket.Packet;
import net.cytonic.protocol.notify.NotifyPacket;

public class PartyInviteExpireNotifyPacket extends NotifyPacket<Packet> {

    @Override
    public String getSubject() {
        return "party.invites.expire";
    }

    public record Packet(
        @SerializedName("request_id")
        UUID request,
        @SerializedName("party_id")
        UUID party,
        @SerializedName("recipient")
        UUID recipient,
        @SerializedName("sender_id")
        UUID sender
    ) implements Message<Packet, Void> {

    }
}
