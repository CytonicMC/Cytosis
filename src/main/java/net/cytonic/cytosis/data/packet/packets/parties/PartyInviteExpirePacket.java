package net.cytonic.cytosis.data.packet.packets.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;

@Getter
@AllArgsConstructor
public class PartyInviteExpirePacket extends RequestPacket<PartyInviteExpirePacket, PartyResponsePacket> {

    @SerializedName("request_id")
    private final UUID request;
    @SerializedName("party_id")
    private final UUID party;
    @SerializedName("recipient")
    private final UUID recipient;
    @SerializedName("sender_id")
    private final UUID sender;

    @Override
    protected Serializer<PartyInviteExpirePacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyInviteExpirePacket.class);
    }

    @Override
    protected Class<PartyResponsePacket> getResponseType() {
        return PartyResponsePacket.class;
    }
}
