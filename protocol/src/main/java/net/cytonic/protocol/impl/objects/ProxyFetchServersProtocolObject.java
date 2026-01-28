package net.cytonic.protocol.impl.objects;

import java.util.List;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.notify.ServerStatusNotifyPacket;
import net.cytonic.protocol.impl.objects.ProxyFetchServersProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.ProxyFetchServersProtocolObject.Response;

public class ProxyFetchServersProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        return "servers.proxy.startup";
    }

    public record Packet() implements Message<Packet, Response> {

    }

    public record Response(List<ServerStatusNotifyPacket.Packet> servers) {

    }
}
