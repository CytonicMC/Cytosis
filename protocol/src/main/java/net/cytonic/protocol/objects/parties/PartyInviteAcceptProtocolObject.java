package net.cytonic.protocol.objects.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.parties.PartyInviteAcceptProtocolObject.Packet;
import net.cytonic.protocol.responses.PartyResponse;

@Internal
public class PartyInviteAcceptProtocolObject extends ProtocolObject<Packet, PartyResponse> {

    @Override
    public String getSubject() {
        return "party.invites.accept";
    }

    @Internal
    public record Packet(@SerializedName("request_id") UUID request) implements Message<Packet, PartyResponse> {

    }
}
