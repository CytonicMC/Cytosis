package net.cytonic.protocol.impl.objects.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyInviteProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.PartyResponse;

public class PartyInviteProtocolObject extends ProtocolObject<Packet, PartyResponse> {

    @Override
    public String getSubject() {
        return "party.invites.send";
    }

    public record Packet(
        @Nullable
        @SerializedName("party_id")
        UUID party,
        @SerializedName("sender_id")
        UUID sender,
        @SerializedName("recipient_id")
        UUID recipient
    ) implements Message<Packet, PartyResponse> {

    }
}
