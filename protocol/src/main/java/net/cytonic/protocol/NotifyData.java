package net.cytonic.protocol;

import io.nats.client.Message;

public record NotifyData(String subject) {

    public NotifyData(Message message) {
        this(message.getSubject());
    }
}
