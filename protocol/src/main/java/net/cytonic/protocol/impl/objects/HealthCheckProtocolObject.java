package net.cytonic.protocol.impl.objects;

import net.minestom.server.codec.Codec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.HealthCheckProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.HealthCheckProtocolObject.Response;
import net.cytonic.protocol.utils.ProtocolCodecUtils;

public class HealthCheckProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException("HealthCheckProtocolObject does not have a default subject!");
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

    public record Response() {

        public static final Codec<Response> CODEC = ProtocolCodecUtils.Unit(new Response());
    }
}
