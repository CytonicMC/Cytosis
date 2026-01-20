package net.cytonic.protocol.objects;

import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket;
import net.cytonic.protocol.objects.FetchServersProtocolObject.Packet;
import net.cytonic.protocol.objects.FetchServersProtocolObject.Response;

@Internal
public class FetchServersProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        return "servers.list";
    }

    @Internal
    public record Packet() implements Message<Packet, Response> {

    }

    @Internal
    public record Response(List<ServerStatusNotifyPacket.Packet> servers) {

    }
}
