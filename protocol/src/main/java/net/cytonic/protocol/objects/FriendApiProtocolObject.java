package net.cytonic.protocol.objects;

import java.time.Instant;
import java.util.UUID;

import net.cytonic.protocol.GsonSerializer;
import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.Serializer;
import net.cytonic.protocol.objects.FriendApiProtocolObject.Packet;
import net.cytonic.protocol.objects.FriendApiProtocolObject.Response;

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
