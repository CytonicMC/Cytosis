package net.cytonic.protocol.impl.objects.games;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.games.PlayProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.games.PlayProtocolObject.Response;

@NoArgsConstructor
@AllArgsConstructor
public class PlayProtocolObject extends ProtocolObject<Packet, Response> {

    private String subject;

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    @Override
    public Codec<Response> getReturnCodec() {
        return Response.CODEC;
    }

    public record Packet(UUID player) implements Message<Packet, Response> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "player", Codec.UUID_STRING, Packet::player,
            Packet::new
        );
    }

    public record Response(Key serverType, @Nullable String error) {

        public static final Codec<Response> CODEC = StructCodec.struct(
            "serverType", Codec.KEY, Response::serverType,
            "error", Codec.STRING.optional(), Response::error,
            Response::new
        );
    }
}
