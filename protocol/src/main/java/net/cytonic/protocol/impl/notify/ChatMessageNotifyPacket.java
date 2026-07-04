package net.cytonic.protocol.impl.notify;

import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.protocol.Message;
import net.cytonic.protocol.NoResponse;
import net.cytonic.protocol.impl.notify.ChatMessageNotifyPacket.Packet;
import net.cytonic.protocol.utils.ProtocolCodecUtils;

public class ChatMessageNotifyPacket extends NoResponse<Packet> {

    @Override
    public String getSubject() {
        return "chat.message";
    }

    @Override
    public Codec<Packet> getCodec() {
        return Packet.CODEC;
    }

    public record Packet(
        @Nullable Set<UUID> recipients,
        String channel,
        Component message,
        @Nullable UUID sender
    ) implements Message<Packet, Void> {

        public static final Codec<Packet> CODEC = StructCodec.struct(
            "recipients", ProtocolCodecUtils.nullSafeOptional(Codec.UUID_STRING.set()), Packet::recipients,
            "channel", Codec.STRING, Packet::channel,
            "message", ProtocolCodecUtils.COMPONENT, Packet::message,
            "sender", Codec.UUID_STRING.optional(), Packet::sender,
            Packet::new
        );

        public Packet(@Nullable Set<UUID> recipients, Enum<?> channel, Component message,
            @Nullable UUID sender) {
            this(recipients, channel.name(), message, sender);
        }
    }
}
