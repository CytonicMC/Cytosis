package net.cytonic.protocol.utils;

import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.nats.client.Message;
import net.kyori.adventure.util.Services;

public interface NatsAPI {

    NatsAPI INSTANCE = Services.service(
        ServiceLoader.load(NatsAPI.class, NatsAPI.class.getClassLoader()),
        NatsAPI.class
    ).orElseThrow();

    void subscribe(String channel, Consumer<Message> consumer);

    void publish(String channel, String data);

    void request(String channel, String data, BiConsumer<byte[], Throwable> consumer);
}
