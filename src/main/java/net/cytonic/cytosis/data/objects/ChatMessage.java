package net.cytonic.cytosis.data.objects;

import java.time.Instant;
import java.util.UUID;

import io.ebean.Model;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.utils.Utils;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "cytonic_chat",
    indexes = {
        @Index(name = "idx_sender", columnList = "sender"),
        @Index(name = "idx_recipient", columnList = "sender"),
        @Index(name = "idx_sent_at", columnList = "sent_at")
    }
)
public class ChatMessage extends Model {

    public static final Codec<ChatMessage> CODEC = StructCodec.struct(
        "id", Codec.LONG, ChatMessage::getId,
        "sentAt", Utils.INSTANT, ChatMessage::getSentAt,
        "channel", Codec.Enum(ChatChannel.class), ChatMessage::getChannel,
        "content", Codec.STRING, ChatMessage::getContent,
        "sender", Codec.UUID_STRING, ChatMessage::getSender,
        "recipient", Codec.UUID_STRING.optional(), ChatMessage::getRecipient,
        ChatMessage::new
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @WhenCreated
    @Column(name = "sent_at")
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    private ChatChannel channel;

    @Column(nullable = false)
    @DbDefault("< Empty Message >")
    private String content;

    @Column(nullable = false)
    private UUID sender;

    @Nullable
    private UUID recipient;
}
