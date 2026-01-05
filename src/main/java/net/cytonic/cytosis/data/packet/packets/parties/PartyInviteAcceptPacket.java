package net.cytonic.cytosis.data.packet.packets.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class PartyInviteAcceptPacket extends RequestPacket<PartyInviteAcceptPacket, PartyResponsePacket> {

    @SerializedName("request_id")
    private final UUID request;

    @Override
    protected Serializer<PartyInviteAcceptPacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyInviteAcceptPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PARTY_INVITE_ACCEPT_REQUEST;
    }

    @Override
    protected Class<PartyResponsePacket> getResponseType() {
        return PartyResponsePacket.class;
    }
}
