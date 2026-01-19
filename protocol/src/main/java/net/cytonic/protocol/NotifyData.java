package net.cytonic.protocol;

import io.nats.client.Message;

public record NotifyData(Message message, String subject) {

    public NotifyData(Message message) {
        this(message, message.getSubject());
    }
}
