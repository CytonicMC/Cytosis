package net.cytonic.protocol.objects.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.objects.parties.PartyOnePlayerProtocolObject.Packet;
import net.cytonic.protocol.responses.PartyResponse;

@NoArgsConstructor
@AllArgsConstructor
@Internal
public class PartyOnePlayerProtocolObject extends ProtocolObject<Packet, PartyResponse> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
    }

    @Internal
    public record Packet(
        @SerializedName("party_id")
        UUID party,
        @SerializedName("player_id")
        UUID player
    ) implements Message<Packet, PartyResponse> {

    }
}
