package net.cytonic.protocol.objects;

import java.util.List;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket;
import net.cytonic.protocol.objects.FetchServersProtocolObject.Packet;
import net.cytonic.protocol.objects.FetchServersProtocolObject.Response;

public class FetchServersProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        return "servers.list";
    }

    public record Packet() implements Message<Packet, Response> {

    }

    public record Response(List<ServerStatusNotifyPacket.Packet> servers) {

    }
}
