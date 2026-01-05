package net.cytonic.cytosis.data.packet.packets.parties;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.parties.Party;

@Getter
@AllArgsConstructor
public class PartyCreatePacket extends RequestPacket<PartyCreatePacket, PartyResponsePacket> {

    private final Party party;

    @Override
    protected Serializer<PartyCreatePacket> getSerializer() {
        return new DefaultGsonSerializer<>(PartyCreatePacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.PARTY_CREATE_NOTIFY;
    }

    @Override
    protected Class<PartyResponsePacket> getResponseType() {
        return PartyResponsePacket.class;
    }
}
