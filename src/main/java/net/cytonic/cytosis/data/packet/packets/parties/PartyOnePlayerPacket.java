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
public class PartyOnePlayerPacket extends ReusableRequestPacket<PartyOnePlayerPacket, PartyResponsePacket> {

    @SerializedName("party_id")
    private final UUID party;
    @SerializedName("player_id")
    private final UUID player;

    @Override
    protected Serializer<PartyOnePlayerPacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyOnePlayerPacket.class);
    }

    @Override
    protected Class<PartyResponsePacket> getResponseType() {
        return PartyResponsePacket.class;
    }
}
