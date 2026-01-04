package net.cytonic.cytosis.data.packet.packets.servers;

import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

public class FetchServersPacket extends Packet<FetchServersPacket> {

    @Override
    protected Serializer<FetchServersPacket> getSerializer() {
        return new DefaultGsonSerializer<>(FetchServersPacket.class);
    }

    @Override
    public String getSubject() {
        return Subjects.SERVER_LIST;
    }
}
