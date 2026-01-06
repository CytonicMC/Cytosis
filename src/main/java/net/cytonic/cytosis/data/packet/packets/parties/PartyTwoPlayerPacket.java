package net.cytonic.cytosis.data.packet.packets.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.ReusableRequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@Setter
@AllArgsConstructor
public class PartyTwoPlayerPacket extends ReusableRequestPacket<PartyTwoPlayerPacket, PartyResponsePacket> {

    @SerializedName("party_id")
    private final UUID party;
    @SerializedName("player_id")
    private final UUID player;
    @SerializedName("sender_id")
    private final UUID sender;

    @Override
    protected Serializer<PartyTwoPlayerPacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyTwoPlayerPacket.class);
    }

    @Override
    protected Class<PartyResponsePacket> getResponseType() {
        return PartyResponsePacket.class;
    }
}
