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
public class PartyLeavePacket extends RequestPacket<PartyLeavePacket, PartyResponsePacket> {

    @SerializedName("player_id")
    private final UUID player;

    @Override
    protected Serializer<PartyLeavePacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyLeavePacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PARTY_LEAVE_REQUEST;
    }

    @Override
    protected Class<PartyResponsePacket> getResponseType() {
        return PartyResponsePacket.class;
    }
}
