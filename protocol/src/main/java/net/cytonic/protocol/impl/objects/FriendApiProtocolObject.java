package net.cytonic.protocol.impl.objects;

import java.time.Instant;
import java.util.UUID;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.impl.objects.FriendApiProtocolObject.Packet;
import net.cytonic.protocol.impl.objects.FriendApiProtocolObject.Response;
import net.cytonic.protocol.serializer.GsonSerializer;
import net.cytonic.protocol.serializer.Serializer;

public class FriendApiProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public Serializer<Packet> getSerializer() {
        return new GsonSerializer<>(Packet.class, GsonSerializer.GO_GSON);
    }

    @Override
    public String getSubject() {
        return "friends.request";
    }

    public record Packet(UUID sender, UUID recipient, Instant expiry) implements Message<Packet, Response> {

    }

    public record Response(boolean success, String code, String message) {

    }
}
