package net.cytonic.protocol.objects;

import java.time.Instant;
import java.util.UUID;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.protocol.GsonSerializer;
import net.cytonic.protocol.Message;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.Serializer;
import net.cytonic.protocol.objects.FriendApiProtocolObject.Packet;
import net.cytonic.protocol.objects.FriendApiProtocolObject.Response;

@Internal
public class FriendApiProtocolObject extends ProtocolObject<Packet, Response> {

    @Override
    public Serializer<Packet> getSerializer() {
        return new GsonSerializer<>(Packet.class, GsonSerializer.GO_GSON);
    }

    @Override
    public String getSubject() {
        return "friends.request";
    }

    @Internal
    public record Packet(UUID sender, UUID recipient, Instant expiry) implements Message<Packet, Response> {

    }

    @Internal
    public record Response(boolean success, String code, String message) {

    }
}
