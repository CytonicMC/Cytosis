package net.cytonic.cytosis.messaging.nats;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.SneakyThrows;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;

import java.util.Objects;

import static io.nats.client.ConnectionListener.Events.*;

public class NatsManager {

    Connection natsConnection;

    @SneakyThrows
    public void setup() {
        ConnectionListener connectionListener = (conn, type) -> {

            Objects.requireNonNull(type);

            if (type == CONNECTED || type == RESUBSCRIBED || type == RECONNECTED) {
                natsConnection = conn;
                Logger.info("Connected asynchronously to NATS server!");
            } else {
                Logger.info("Disconnected from NATS server!");
                natsConnection = null;
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
        natsConnection.close();
    }

    public void sendStartup() {


    }


}
