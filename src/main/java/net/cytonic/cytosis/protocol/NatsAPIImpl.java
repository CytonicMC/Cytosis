package net.cytonic.cytosis.protocol;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.nats.client.Message;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.protocol.utils.NatsAPI;

@CytosisComponent(dependsOn = NatsManager.class)
public class NatsAPIImpl implements NatsAPI {

    @Override
    public void subscribe(String channel, Consumer<Message> consumer) {
        log("Subscribing to channel %s", channel);
        Cytosis.get(NatsManager.class).subscribe(channel, message -> {
            log("Received Nats Message from sub %s %s", channel, new String(message.getData()));
            consumer.accept(message);
        });
    }

    @Override
    public void publish(String channel, String data) {
        log("Publishing to channel %s %s", channel, data);
        Cytosis.get(NatsManager.class).publish(channel, data.getBytes());
    }

    @Override
    public void request(String channel, String data, BiConsumer<byte[], Throwable> consumer) {
        log("Requesting on channel %s %s", channel, data);
        Cytosis.get(NatsManager.class).request(channel, data.getBytes(), ((message, throwable) -> {
            log("Received Nats request from request %s %s", channel, new String(message.getData()));
            consumer.accept(message.getData(), throwable);
        }));
    }

    private static void log(String message, Object... args) {
        if (Boolean.getBoolean("nats_debug")) {
            Logger.debug(message, args);
        }
    }
}
