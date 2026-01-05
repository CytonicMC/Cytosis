package net.cytonic.cytosis.data.packet.packets.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.IllegalSubjectException;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@Setter
@AllArgsConstructor
public class PartyStatePacket extends RequestPacket<PartyStatePacket, PartyResponsePacket> {

    @SerializedName("party_id")
    private final UUID party;
    @SerializedName("player_id")
    private final UUID player;
    @SerializedName("state")
    private final boolean state;

    @Override
    protected Serializer<PartyStatePacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyStatePacket.class);
    }

    @Override
    public String getSubject() {
        throw new IllegalSubjectException();
    }

    @Override
    protected Class<PartyResponsePacket> getResponseType() {
        return PartyResponsePacket.class;
    }
}
