package net.cytonic.cytosis.messaging.nats;

import io.nats.client.*;
import lombok.SneakyThrows;
import net.cytonic.containers.ServerStatusContainer;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.Utils;

import java.time.Instant;
import java.util.Objects;

import static io.nats.client.ConnectionListener.Events.*;

public class NatsManager {


    Connection connection;
    Subscription healthCheck;

    @SneakyThrows
    public void setup() {
        ConnectionListener connectionListener = (conn, type) -> {

            Objects.requireNonNull(type);

            if (type == CONNECTED || type == RESUBSCRIBED || type == RECONNECTED) {
                connection = conn;
                Logger.info("Connected asynchronously to NATS server!");
                startHealthCheck();
            } else {
                Logger.info("Disconnected from NATS server!");
                connection = null;
            }
        };

        Options options = Options.builder()
//                .server("nats://"+ CytosisSettings.NATS_USERNAME +":" + CytosisSettings.NATS_PASSWORD + "@" + CytosisSettings.NATS_HOSTNAME + ":" + CytosisSettings.NATS_PORT) // todo: probably get away from string templates
                .server(STR."nats://\{CytosisSettings.NATS_USERNAME}:\{CytosisSettings.NATS_PASSWORD}@\{CytosisSettings.NATS_HOSTNAME}:\{CytosisSettings.NATS_PORT}")
                .connectionListener(connectionListener)
                .build();
        Nats.connectAsynchronously(options, true);
    }

    @SneakyThrows // don't care about the error on shutdown
    public void shutdown() {
        sendShutdown();
        connection.close();
    }

    public void sendStartup() {
        byte[] data = new ServerStatusContainer("TYPE_HERE", Utils.getServerIP(), Cytosis.getRawID(), CytosisSettings.SERVER_PORT, null).serialize().getBytes();
        Thread.ofVirtual().name("NATS Startup Publisher").start(() -> {
                    try {
                        connection.publish(Subjects.SERVER_REGISTER, data);
                    } catch (Exception e) {
                        Logger.error("Failed to send STARTUP", e);
                    }
                }
        );
    }

    public void sendShutdown() {
        byte[] data = new ServerStatusContainer("TYPE_HERE", Utils.getServerIP(), Cytosis.getRawID(), CytosisSettings.SERVER_PORT, Instant.now()).serialize().getBytes();
        Thread.ofVirtual().name("NATS Shutdown Publisher").start(() ->
                connection.publish(Subjects.SERVER_SHUTDOWN, data));
    }

    public void startHealthCheck() {
        if (healthCheck != null) {
            healthCheck.getDispatcher().unsubscribe(healthCheck);
        }
        Dispatcher dispatcher = connection.createDispatcher();
        healthCheck = dispatcher.subscribe(Subjects.HEALTH_CHECK, msg -> {
            // reply
            connection.publish(msg.getReplyTo(), new byte[0]);
        });
    }

    public void listenForFriends() {
        Thread.ofVirtual().name("NATS Friend Expiry Worker").start(() -> {

        });
    }
}
