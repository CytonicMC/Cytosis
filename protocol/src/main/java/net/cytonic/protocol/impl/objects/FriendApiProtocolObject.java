package net.cytonic.protocol.impl.objects;

import java.time.Instant;
import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.FriendApiProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.FriendApiProtocolObject.Response;
import net.cytonic.protocol.utils.ProtocolCodecUtils;

public class FriendApiProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        return "friends.request";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<Response> getReturnCodec() {
        return Response.CODEC;
    }

    public record Packet(UUID sender, UUID recipient, Instant expiry) implements Message<Packet, Response> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "sender", Codec.UUID_STRING, Packet::sender,
            "recipient", Codec.UUID_STRING, Packet::recipient,
            "expiry", ProtocolCodecUtils.INSTANT, Packet::expiry,
            Packet::new
        );
    }

    public record Response(boolean success, String code, String message) {

        public static final Codec<Response> CODEC = StructCodec.struct(
            "success", Codec.BOOLEAN, Response::success,
            "code", Codec.STRING, Response::code,
            "message", Codec.STRING, Response::message,
            Response::new
        );
    }
}
