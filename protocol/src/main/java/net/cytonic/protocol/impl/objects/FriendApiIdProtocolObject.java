package net.cytonic.protocol.impl.objects;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.FriendApiProtocolObject.Response;

@NoArgsConstructor
@AllArgsConstructor
public class FriendApiIdProtocolObject extends
    ProtocolObject<FriendApiIdProtocolObject.Packet, FriendApiProtocolObject.Response> {

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

    public record Packet(
        UUID requestId
    ) implements Message<FriendApiIdProtocolObject, FriendApiProtocolObject.Response> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "request_id", Codec.UUID_STRING, Packet::requestId,
            Packet::new
        );
    }
}
