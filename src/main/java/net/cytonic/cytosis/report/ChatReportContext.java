package net.cytonic.cytosis.report;

import java.util.List;

import lombok.Getter;
import org.jetbrains.annotations.Unmodifiable;

import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.objects.ChatMessage;
import net.cytonic.cytosis.report.ReportTypes.ChatReport;

@Getter
public class ChatReportContext extends ReportContext<ChatReport> {

    private final ChatChannel channel;
    private final List<ChatMessage> messages;

    public ChatReportContext(@Unmodifiable List<ChatMessage> chatMessages, ChatChannel channel) {
        this.messages = chatMessages;
        this.channel = channel;
    }
}
