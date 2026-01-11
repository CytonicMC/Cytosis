package net.cytonic.cytosis.messaging;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Dispatcher;
import io.nats.client.ErrorListener;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;
import lombok.SneakyThrows;
import net.minestom.server.MinecraftServer;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.config.CytosisSettings.NatsConfig;
import net.cytonic.cytosis.environments.EnvironmentManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.protocol.listeners.ServerStatusNotifyListener;
import net.cytonic.cytosis.utils.Utils;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket;
import net.cytonic.protocol.notifyPackets.ServerStatusNotifyPacket.Type;
import net.cytonic.protocol.objects.FetchServersProtocolObject;
import net.cytonic.protocol.objects.HealthCheckProtocolObject;

import static io.nats.client.ConnectionListener.Events.CONNECTED;
import static io.nats.client.ConnectionListener.Events.RECONNECTED;
import static io.nats.client.ConnectionListener.Events.RESUBSCRIBED;

@CytosisComponent(dependsOn = {CytonicNetwork.class, EnvironmentManager.class})
public class NatsManager implements Bootstrappable {

    private final ConcurrentLinkedDeque<PublishContainer> publishQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<RequestContainer> requestQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<SubscribeContainer> subscribeQueue = new ConcurrentLinkedDeque<>();
    private CytosisSettings cytosisSettings;
    private Connection connection;
    private Subscription healthCheck;
    private boolean started = false;

    @Override
    public void init() {
        this.cytosisSettings = Cytosis.get(CytosisSettings.class);

        if (!Cytosis.CONTEXT.getFlags().contains("--ci-test")) {
            setup();
            fetchServers();
        } else {
            Logger.warn("Skipping NATS manager setup for CI test!");
        }

        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            Logger.info("Registering server with Cydian!");
            createServerStatusPacket(true).publish();
        });
    }

    @SneakyThrows // don't care about the error on shutdown
    @Override
    public void shutdown() {
        createServerStatusPacket(false).publish();
        connection.close();
    }

    private ServerStatusNotifyPacket.Packet createServerStatusPacket(boolean isStartup) {
        return new ServerStatusNotifyPacket.Packet(
            Cytosis.CONTEXT.getServerGroup().type(),
            Utils.getServerIP(),
            CytosisContext.SERVER_ID,
            Cytosis.get(CytosisSettings.class).getServerConfig().getPort(),
            Instant.now(),
            Cytosis.CONTEXT.getServerGroup().group(),
            isStartup ? Type.STARTUP : Type.SHUTDOWN
        );
    }

    private void fetchServers() {
        new FetchServersProtocolObject.Packet().request((response, throwable) -> {
            if (throwable != null) {
                Logger.error("failed to fetch active servers!", throwable);
                return;
            }

            for (ServerStatusNotifyPacket.Packet server : response.servers()) {
                Cytosis.get(CytonicNetwork.class).getServers()
                    .put(server.id(), ServerStatusNotifyListener.getServer(server));
                Logger.info("Loaded server '" + server.id() + "' from Cydian!");
            }
            Logger.info("Loaded " + response.servers().size() + " active servers from Cydian!");
        });
    }

    @SneakyThrows
    public void setup() {
        NatsConfig natsConfig = cytosisSettings.getNatsConfig();
        Options options = Options.builder().server(
                "nats://" + natsConfig.getUser() + ":" + natsConfig.getPassword() + "@"
                    + natsConfig.getHost() + ":" + natsConfig.getPort())
            .connectionListener(setupConnectionListener()).errorListener(new ErrorListener() {
                @Override
                public void errorOccurred(Connection conn, String error) {
                    Logger.error("An error occurred in a NATS action: %s in connection %s", error,
                        conn.getServerInfo().getClientId());
                }
            }).build();
        Nats.connectAsynchronously(options, true);
    }

    private ConnectionListener setupConnectionListener() {
        return (conn, type) -> {

            if (type == CONNECTED || type == RESUBSCRIBED || type == RECONNECTED) {
                connection = conn;
                Logger.info("Connected asynchronously to NATS server!");
                startHealthCheck();

                PublishContainer publish;
                while ((publish = publishQueue.poll()) != null) {
                    connection.publish(publish.channel, publish.data);
                }

                RequestContainer request;
                while ((request = requestQueue.poll()) != null) {
                    connection.request(request.channel, request.data).whenComplete(request.consumer);
                }

                SubscribeContainer subscribe;
                while ((subscribe = subscribeQueue.poll()) != null) {
                    connection.createDispatcher(subscribe.consumer::accept).subscribe(subscribe.channel);
                }

                if (!started) {
                    started = true;
                }
            } else {
                Logger.info("Disconnected from NATS server! (%s)", type.name());
                connection = null;
            }
        };
    }

    public void startHealthCheck() {
        if (healthCheck != null) {
            healthCheck.getDispatcher().unsubscribe(healthCheck);
        }
        Dispatcher dispatcher = connection.createDispatcher();
        healthCheck = dispatcher.subscribe(Subjects.applyPrefix(Subjects.HEALTH_CHECK),
            msg -> new HealthCheckProtocolObject.Packet().publish(msg.getReplyTo()));
    }

    /**
     * Queues publishing until the nats connection is completed.
     *
     * @param channel The channel to publish on
     * @param data    The data to publish
     */
    public void publish(String channel, byte[] data) {
        channel = Subjects.applyPrefix(channel);
        if (connection != null) {
            connection.publish(channel, data);
            return;
        }

        publishQueue.add(new PublishContainer(channel, data));
    }

    public void request(String channel, byte[] data, BiConsumer<Message, Throwable> consumer) {
        channel = Subjects.applyPrefix(channel);
        if (connection != null) {
            connection.request(channel, data).whenComplete(consumer);
            return;
        }

        requestQueue.add(new RequestContainer(channel, data, consumer));
    }

    public void subscribe(String channel, Consumer<Message> consumer) {
        channel = Subjects.applyPrefix(channel);
        if (connection != null) {
            connection.createDispatcher(consumer::accept).subscribe(channel);
            return;
        }

        subscribeQueue.add(new SubscribeContainer(channel, consumer));
    }

    private record PublishContainer(String channel, byte[] data) {

    }

    private record RequestContainer(String channel, byte[] data, BiConsumer<Message, Throwable> consumer) {

    }

    private record SubscribeContainer(String channel, Consumer<Message> consumer) {

    }
}
