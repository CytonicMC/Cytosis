package net.cytonic.protocol.impl.notify;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.notify.FriendNotifyPacket.Packet;
import net.cytonic.protocol.impl.objects.FriendApiProtocolObject.Response;
import net.cytonic.protocol.utils.NatsAPI;

@NoArgsConstructor
@AllArgsConstructor
public class FriendNotifyPacket extends ProtocolObject<Packet, Response> {

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

    public record Packet(UUID sender, UUID recipient) implements Message<Packet, Response> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "sender", Codec.UUID_STRING, Packet::sender,
            "recipient", Codec.UUID_STRING, Packet::recipient,
            Packet::new
        );

        @Override
        public void publish(String subject) {
            NatsAPI.INSTANCE.publish(subject, getProtocolObject().serializeToString(this));
        }
    }
}
