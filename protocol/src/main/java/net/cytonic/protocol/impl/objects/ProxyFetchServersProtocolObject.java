package net.cytonic.protocol.impl.objects;

import java.util.List;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.notify.ServerStatusNotifyPacket;
import net.cytonic.protocol.impl.objects.ProxyFetchServersProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.ProxyFetchServersProtocolObject.Response;
import net.cytonic.protocol.utils.ProtocolCodecUtils;

public class ProxyFetchServersProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        return "servers.proxy.startup";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<Response> getReturnCodec() {
        return Response.CODEC;
    }

    public record Packet() implements Message<Packet, Response> {

        public static final Codec<Packet> CODEC = ProtocolCodecUtils.Unit(new Packet());
    }

    public record Response(List<ServerStatusNotifyPacket.Packet> servers) {

        public static final Codec<Response> CODEC = StructCodec.struct(
            "servers", ServerStatusNotifyPacket.Packet.CODEC.list(), Response::servers,
            Response::new
        );
    }
}
