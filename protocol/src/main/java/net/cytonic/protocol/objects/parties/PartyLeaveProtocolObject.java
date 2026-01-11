package net.cytonic.protocol.objects.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.parties.PartyLeaveProtocolObject.Packet;
import net.cytonic.protocol.responses.PartyResponse;

public class PartyLeaveProtocolObject extends ProtocolObject<Packet, PartyResponse> {

    @Override
    public String getSubject() {
        return "party.leave.request";
    }

    public record Packet(@SerializedName("player_id") UUID player) implements Message<Packet, PartyResponse> {

        @Override
        public ProtocolObject<Packet, PartyResponse> getProtocolObject() {
            return new PartyLeaveProtocolObject();
        }
    }
}
