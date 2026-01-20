package net.cytonic.protocol.impl.objects.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.parties.PartyOnePlayerProtocolObject.Packet;
import net.cytonic.protocol.impl.responses.PartyResponse;

@NoArgsConstructor
@AllArgsConstructor
public class PartyOnePlayerProtocolObject extends ProtocolObject<Packet, PartyResponse> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
    }

    public record Packet(
        @SerializedName("party_id")
        UUID party,
        @SerializedName("player_id")
        UUID player
    ) implements Message<Packet, PartyResponse> {

    }
}
