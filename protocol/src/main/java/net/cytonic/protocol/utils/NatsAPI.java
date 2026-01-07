package net.cytonic.protocol.utils;

import java.util.function.Consumer;

import io.nats.client.Message;
import net.kyori.adventure.util.Services;

public interface NatsAPI {

    NatsAPI INSTANCE = Services.service(NatsAPI.class).orElseThrow();

    void subscribe(String channel, Consumer<Message> consumer);

    void publish(String channel, String data);

    void request(String channel, String data, Consumer<byte[]> consumer);
}
