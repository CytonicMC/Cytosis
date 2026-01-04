package net.cytonic.cytosis.data.packet.packets.parties;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.parties.PartyInvite;

@Getter
@AllArgsConstructor
public class PartyInvitePacket extends Packet<PartyInvitePacket> {

    @SerializedName("party_invite")
    private final PartyInvite partyInvite;

    @Override
    protected Serializer<PartyInvitePacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyInvitePacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PARTY_INVITE_SEND_NOTIFY;
    }
}
