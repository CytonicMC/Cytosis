package net.cytonic.protocol.objects.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.parties.PartyStateProtocolObject.Packet;
import net.cytonic.protocol.responses.PartyResponse;

@NoArgsConstructor
@AllArgsConstructor
public class PartyStateProtocolObject extends ProtocolObject<Packet, PartyResponse> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
    }

    public record Packet(@SerializedName("party_id") UUID party,
                         @SerializedName("player_id") UUID player,
                         @SerializedName("state") boolean state
    ) implements Message<Packet, PartyResponse> {

    }
}
