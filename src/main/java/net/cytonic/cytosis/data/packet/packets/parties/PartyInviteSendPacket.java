package net.cytonic.cytosis.data.packet.packets.parties;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

@Getter
@AllArgsConstructor
public class PartyInviteSendPacket extends RequestPacket<PartyInviteSendPacket, PartyResponsePacket> {

    @Nullable
    @SerializedName("party_id")
    private final UUID party;
    @SerializedName("sender_id")
    private final UUID sender;
    @SerializedName("recipient_id")
    private final UUID recipient;

    @Override
    protected Serializer<PartyInviteSendPacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyInviteSendPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PARTY_INVITE_SEND_REQUEST;
    }

    @Override
    protected Class<PartyResponsePacket> getResponseType() {
        return PartyResponsePacket.class;
    }
}
