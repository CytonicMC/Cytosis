package net.cytonic.protocol.impl.objects;

import java.util.UUID;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.AccountLinkProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.AccountLinkProtocolObject.Response;

public class AccountLinkProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public String getSubject() {
        return "player.link";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<Response> getReturnCodec() {
        return Response.CODEC;
    }

    public record Packet(UUID uuid) implements Message<Packet, Response> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "uuid", Codec.UUID_STRING, Packet::uuid,
            Packet::new
        );
    }

    public record Response(String token, @Nullable String error) {

        public static final Codec<Response> CODEC = StructCodec.struct(
            "token", Codec.STRING, Response::token,
            "error", Codec.STRING.optional(), Response::error,
            Response::new
        );
    }
}
