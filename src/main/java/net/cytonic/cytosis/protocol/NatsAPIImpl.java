package net.cytonic.cytosis.protocol;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.protocol.ProtocolHelper;
import net.cytonic.protocol.utils.NatsAPI;

@Slf4j
@CytosisComponent(dependsOn = NatsManager.class)
public class NatsAPIImpl implements NatsAPI, Bootstrappable {

    @Override
    public void init() {
        Logger.debug("Initializing Nats API");
        ProtocolHelper.init();
        Logger.debug("After");
    }

    @Override
    public void subscribe(String channel, Consumer<Message> consumer) {
        log.info("Subscribing to channel {}", channel);
        Cytosis.get(NatsManager.class).subscribe(channel, message -> {
            log.info("Received Nats Message from sub {}", new String(message.getData()));
            consumer.accept(message);
        });
    }

    @Override
    public void publish(String channel, String data) {
        log.info("Publishing to channel {} {}", channel, data);
        Cytosis.get(NatsManager.class).publish(channel, data.getBytes());
    }

    @Override
    public void request(String channel, String data, BiConsumer<byte[], Throwable> consumer) {
        log.info("Requesting on channel {}", channel);
        Cytosis.get(NatsManager.class).request(channel, data.getBytes(), ((message, throwable) -> {
            log.info("Received Nats request from request {} {}", channel, new String(message.getData()));
            consumer.accept(message.getData(), throwable);
        }));
    }
}
