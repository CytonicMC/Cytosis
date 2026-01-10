package net.cytonic.cytosis.data.packet.packets.servers;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.RequestPacket;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

public class FetchServersPacket extends RequestPacket<FetchServersPacket, FetchServersResponsePacket> {

    @Override
    protected Serializer<FetchServersPacket> getSerializer() {
        return new DefaultGsonSerializer<>(FetchServersPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.SERVER_LIST;
    }

    @Override
    protected Class<FetchServersResponsePacket> getResponseType() {
        return FetchServersResponsePacket.class;
    }
}
