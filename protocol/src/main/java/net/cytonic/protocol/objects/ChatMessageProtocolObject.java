package net.cytonic.protocol.objects;

import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.GsonSerializer;
import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.ProtocolObject;
import net.cytonic.protocol.Serializer;
import net.cytonic.protocol.data.objects.JsonComponent;
import net.cytonic.protocol.objects.ChatMessageProtocolObject.Packet;

public class ChatMessageProtocolObject extends NoResponse<Packet> {

    @Override
    public Serializer<Packet> getSerializer() {
        return new GsonSerializer<>(Packet.class);
    }

    @Override
    public String getSubject() {
        return "chat.message";
    }

    public record Packet(
        @Nullable Set<UUID> recipients,
        String channel,
        JsonComponent message,
        @Nullable UUID sender) implements Message<Packet, Void> {

        public Packet(@Nullable Set<UUID> recipients, Enum<?> channel, JsonComponent message,
            @Nullable UUID sender) {
            this(recipients, channel.name(), message, sender);
        }

        @Override
        public ProtocolObject<Packet, Void> getProtocolObject() {
            return new ChatMessageProtocolObject();
        }
    }
}
