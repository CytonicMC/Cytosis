package net.cytonic.cytosis.data.packet.packets;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.objects.JsonComponent;
import net.cytonic.cytosis.data.packet.utils.DefaultGsonSerializer;
import net.cytonic.cytosis.data.packet.utils.Packet;
import net.cytonic.cytosis.data.packet.utils.Serializer;
import net.cytonic.cytosis.messaging.Subjects;

/**
 * A class that represents a message sent to the specified recipients.
 * <br><br>
 * <strong>Api Note:</strong> The {@code recipients} field may be null, indicating the message should be broadcast out
 * on the channel, rather than sent only to the recipients.
 */
@Getter
public class ChatMessagePacket extends Packet<ChatMessagePacket> {

    private final @Nullable List<UUID> recipients;
    private final ChatChannel channel;
    private final JsonComponent message;
    private final @Nullable UUID sender;

    /**
     * @param recipients The intended recipients of the message may be null. See the note above.
     * @param channel    The channel the message is sent out upon.
     * @param message    The message
     * @param sender     The player who sent the message
     */
    public ChatMessagePacket(@Nullable List<UUID> recipients, ChatChannel channel, JsonComponent message,
        @Nullable UUID sender) {
        this.recipients = recipients;
        this.channel = channel;
        this.message = message;
        this.sender = sender;
    }

    @Override
    public String getSubject() {
        return Subjects.CHAT_MESSAGE;
    }

    @Override
    protected Serializer<ChatMessagePacket> getSerializer() {
        return new DefaultGsonSerializer<>(ChatMessagePacket.class);
    }
}
