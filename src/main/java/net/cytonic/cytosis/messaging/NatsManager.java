package net.cytonic.cytosis.messaging;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.containers.PlayerKickContainer;
import net.cytonic.cytosis.data.containers.PlayerLoginLogoutContainer;
import net.cytonic.cytosis.data.containers.PlayerRankUpdateContainer;
import net.cytonic.cytosis.data.containers.ServerStatusContainer;
import net.cytonic.cytosis.data.containers.friends.FriendApiResponse;
import net.cytonic.cytosis.data.containers.friends.FriendRequest;
import net.cytonic.cytosis.data.containers.friends.FriendResponse;
import net.cytonic.cytosis.data.containers.friends.OrganicFriendResponse;
import net.cytonic.cytosis.data.containers.servers.PlayerChangeServerContainer;
import net.cytonic.cytosis.data.containers.servers.SendPlayerToServerContainer;
import net.cytonic.cytosis.data.containers.servers.SendToServerTypeContainer;
import net.cytonic.cytosis.data.containers.servers.ServerSendReponse;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.enums.KickReason;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.ChatMessage;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.events.network.PlayerJoinNetworkEvent;
import net.cytonic.cytosis.events.network.PlayerLeaveNetworkEvent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

import static io.nats.client.ConnectionListener.Events.CONNECTED;
import static io.nats.client.ConnectionListener.Events.RECONNECTED;
import static io.nats.client.ConnectionListener.Events.RESUBSCRIBED;

public class NatsManager implements Bootstrappable {

    private static final Component FRIEND_LINE = Msg.darkAqua(
        "<st>                                                                                 ");
    private final ConcurrentLinkedDeque<PublishContainer> publishQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<RequestContainer> requestQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<SubscribeContainer> subscribeQueue = new ConcurrentLinkedDeque<>();
    private RankManager rankManager;
    private FriendManager friendManager;
    private PreferenceManager preferenceManager;
    private CytonicNetwork network;
    private ChatManager chatManager;
    private MysqlDatabase databaseManager;
    private Connection connection;
    private Subscription healthCheck;
    private boolean started = false;

    @Override
    public void init() {
        this.rankManager = Cytosis.CONTEXT.getComponent(RankManager.class);
        this.friendManager = Cytosis.CONTEXT.getComponent(FriendManager.class);
        this.preferenceManager = Cytosis.CONTEXT.getComponent(PreferenceManager.class);
        this.network = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
        this.chatManager = Cytosis.CONTEXT.getComponent(ChatManager.class);
        this.databaseManager = Cytosis.CONTEXT.getComponent(MysqlDatabase.class);
    }

    @SneakyThrows // don't care about the error on shutdown
    @Override
    public void shutdown() {
        sendShutdown();
        connection.close();
    }

    @SneakyThrows
    public void setup() {
        Options options = Options.builder().server(
                "nats://" + CytosisSettings.NATS_USERNAME + ":" + CytosisSettings.NATS_PASSWORD + "@"
                    + CytosisSettings.NATS_HOSTNAME + ":" + CytosisSettings.NATS_PORT)
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
                    listenForFriendDeclineNotifications();
                    listenForFriendAcceptNotifications();
                    listenForFriendExpiryNotifcations();
                    listenForPlayerLoginLogout();
                    listenForFriendRequestNotification();
                    listenForFriendRemoval();
                    listenForServerStatus();
                    listenForPlayerServerChange();
                    listenForChatMessage();
                    listenForPlayerRankUpdates();
                }
            } else {
                Logger.info("Disconnected from NATS server! (%s)", type.name());
                connection = null;
            }
        };
    }

    public void sendStartup() {
        byte[] data = new ServerStatusContainer(Cytosis.CONTEXT.getServerGroup().type(), Utils.getServerIP(),
            CytosisContext.SERVER_ID, CytosisSettings.SERVER_PORT, Instant.now(),
            Cytosis.CONTEXT.getServerGroup().group()).serialize().getBytes();
        Thread.ofVirtual().name("NATS Startup Publisher").start(() -> {
            try {
                Logger.info("Registering server with Cydian!");
                publish(Subjects.SERVER_REGISTER, data);
            } catch (Exception e) {
                Logger.error("Failed to send STARTUP", e);
            }
        });
    }

    public void sendShutdown() {
        byte[] data = new ServerStatusContainer(Cytosis.CONTEXT.getServerGroup().type(), Utils.getServerIP(),
            CytosisContext.SERVER_ID, CytosisSettings.SERVER_PORT, Instant.now(),
            Cytosis.CONTEXT.getServerGroup().group()).serialize().getBytes();
        // send it sync, so the connection doesn't get closed
        publish(Subjects.SERVER_SHUTDOWN, data);
    }

    public void startHealthCheck() {
        if (healthCheck != null) {
            healthCheck.getDispatcher().unsubscribe(healthCheck);
        }
        Dispatcher dispatcher = connection.createDispatcher();
        healthCheck = dispatcher.subscribe(Subjects.HEALTH_CHECK, msg -> {
            // reply
            publish(msg.getReplyTo(), new byte[0]);
        });
    }

    private void listenForFriendDeclineNotifications() {
        connection.createDispatcher().subscribe(Subjects.FRIEND_DECLINATION_NOTIFY, msg -> {

            FriendRequest request = FriendRequest.deserialize(new String(msg.getData()));

            String targetName = network.getLifetimePlayers().getByKey(request.recipient());
            String senderName = network.getLifetimePlayers().getByKey(request.sender());
            PlayerRank targetRank = network.getCachedPlayerRanks().get(request.recipient());
            PlayerRank senderRank = network.getCachedPlayerRanks().get(request.sender());

            Component target = targetRank.getPrefix().append(Component.text(targetName));
            Component sender = senderRank.getPrefix().append(Component.text(senderName));

            for (Player player : Cytosis.getOnlinePlayers()) {
                if (player.getUuid().equals(request.recipient())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(
                        Msg.mm("<red>You declined ").append(sender).append(Msg.mm("<red>'s friend request!")));
                    player.sendMessage(FRIEND_LINE);
                } else if (player.getUuid().equals(request.sender())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(target.append(Msg.mm("<red> declined your friend request!")));
                    player.sendMessage(FRIEND_LINE);
                }
            }
            msg.ack();
        });
    }

    private void listenForFriendAcceptNotifications() {
        connection.createDispatcher().subscribe(Subjects.FRIEND_ACCEPTANCE_NOTIFY, msg -> {
            FriendRequest request = FriendRequest.deserialize(new String(msg.getData()));

            String targetName = network.getLifetimePlayers().getByKey(request.recipient());
            String senderName = network.getLifetimePlayers().getByKey(request.sender());
            PlayerRank targetRank = network.getCachedPlayerRanks().get(request.recipient());
            PlayerRank senderRank = network.getCachedPlayerRanks().get(request.sender());

            Component target = targetRank.getPrefix().append(Component.text(targetName));
            Component sender = senderRank.getPrefix().append(Component.text(senderName));

            for (Player player : Cytosis.getOnlinePlayers()) {
                if (player.getUuid().equals(request.recipient())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(
                        Msg.mm("<aqua>You accepted ").append(sender).append(Msg.mm("<aqua>'s friend request!")));
                    player.sendMessage(FRIEND_LINE);

                    // this server gets the authority to add them as a friend in the database
                    friendManager.addFriend(request.sender(), request.recipient());
                } else if (player.getUuid().equals(request.sender())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(target.append(Msg.mm("<aqua> accepted your friend request!")));
                    player.sendMessage(FRIEND_LINE);
                }
            }
            msg.ack();
        });
    }

    private void listenForFriendExpiryNotifcations() {
        connection.createDispatcher().subscribe(Subjects.FRIEND_EXPIRE_NOTIFY, msg -> {

            FriendRequest request = FriendRequest.deserialize(new String(msg.getData()));

            String targetName = network.getLifetimePlayers().getByKey(request.recipient());
            String senderName = network.getLifetimePlayers().getByKey(request.sender());
            PlayerRank targetRank = network.getCachedPlayerRanks().get(request.recipient());
            PlayerRank senderRank = network.getCachedPlayerRanks().get(request.sender());

            Component target = targetRank.getPrefix().append(Component.text(targetName));
            Component sender = senderRank.getPrefix().append(Component.text(senderName));

            for (Player player : Cytosis.getOnlinePlayers()) {
                if (player.getUuid().equals(request.recipient())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(
                        Msg.aqua("Your friend request from ").append(sender).append(Msg.aqua(" has expired!")));
                    player.sendMessage(FRIEND_LINE);
                } else if (player.getUuid().equals(request.sender())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(
                        Msg.aqua("Your friend request to ").append(target).append(Msg.aqua(" has expired!")));
                    player.sendMessage(FRIEND_LINE);
                }
            }
            msg.ack();
        });
    }

    private void listenForPlayerLoginLogout() {
        connection.createDispatcher(msg -> {
            var container = PlayerLoginLogoutContainer.deserialize(new String(msg.getData()));
            EventDispatcher.call(new PlayerJoinNetworkEvent(container.uuid(), container.username()));
            network.addPlayer(container.username(), container.uuid());
            preferenceManager.loadPlayerPreferences(container.uuid());
            friendManager.sendLoginMessage(container.uuid());
            rankManager.loadPlayer(container.uuid());
        }).subscribe(Subjects.PLAYER_JOIN);
        connection.createDispatcher(msg -> {
            var container = PlayerLoginLogoutContainer.deserialize(new String(msg.getData()));
            EventDispatcher.call(new PlayerLeaveNetworkEvent(container.uuid(), container.username()));
            network.removePlayer(container.username(), container.uuid());
            preferenceManager.unloadPlayerPreferences(container.uuid());
            friendManager.sendLogoutMessage(container.uuid());
            rankManager.removePlayer(container.uuid());
        }).subscribe(Subjects.PLAYER_LEAVE);
    }

    private void listenForFriendRequestNotification() {
        connection.createDispatcher(msg -> {
            FriendRequest request = FriendRequest.deserialize(new String(msg.getData()));

            String targetName = network.getLifetimePlayers().getByKey(request.recipient());
            String senderName = network.getLifetimePlayers().getByKey(request.sender());
            PlayerRank targetRank = network.getCachedPlayerRanks().get(request.recipient());
            PlayerRank senderRank = network.getCachedPlayerRanks().get(request.sender());
            Component target = targetRank.getPrefix().append(Component.text(targetName));
            Component sender = senderRank.getPrefix().append(Component.text(senderName));

            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                if (request.recipient().equals(player.getUuid())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(sender.append(
                        Msg.mm("<aqua> sent you a friend request! You have 5 minutes to accept it! ").append(Msg.mm(
                            "<green><b><click:run_command:/friend accept " + request.sender()
                                + ">[Accept]</click></b></green> <red><b><click:run_command:/friend decline "
                                + request.sender() + ">[Decline]</click></b></red>"))));
                    player.sendMessage(FRIEND_LINE);
                } else if (request.sender().equals(player.getUuid())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(Msg.mm("<aqua>You send a friend request to ").append(target)
                        .append(Msg.mm("<aqua>! They have 5 minutes to accept it!")));
                    player.sendMessage(FRIEND_LINE);
                }
            }
        }).subscribe(Subjects.FRIEND_REQUEST_NOTIFY);
    }

    private void listenForFriendRemoval() {
        connection.createDispatcher(msg -> {
            Tuple<UUID, UUID> tuple = Tuple.deserialize(new String(msg.getData()), Tuple.UUID_TYPE);

            String targetName = network.getLifetimePlayers().getByKey(tuple.getSecond());
            String senderName = network.getLifetimePlayers().getByKey(tuple.getFirst());
            PlayerRank targetRank = network.getCachedPlayerRanks().get(tuple.getSecond());
            PlayerRank senderRank = network.getCachedPlayerRanks().get(tuple.getFirst());
            Component target = targetRank.getPrefix().append(Component.text(targetName));
            Component sender = senderRank.getPrefix().append(Component.text(senderName));

            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                if (player.getUuid().equals(tuple.getSecond())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(sender.append(Msg.mm("<aqua> removed you from their friend list!")));
                    player.sendMessage(FRIEND_LINE);
                } else if (player.getUuid().equals(tuple.getFirst())) {
                    player.sendMessage(FRIEND_LINE);
                    player.sendMessage(
                        Msg.mm("<aqua>You removed ").append(target).append(Msg.mm(" from your friend list!")));
                    player.sendMessage(FRIEND_LINE);
                }
            }

        }).subscribe(Subjects.FRIEND_REMOVED);
    }

    private void listenForServerStatus() {
        connection.createDispatcher(msg -> {
            var container = ServerStatusContainer.deserialize(new String(msg.getData()));
            network.getServers().put(container.id(), container.server());
            Logger.info("Registered server: " + container.id());

            Cytosis.getOnlinePlayers().forEach(player -> {
                if (!player.isAdmin()) return;
                if (player.getPreference(CytosisPreferences.SERVER_ALERTS)) {
                    player.sendMessage(
                        Msg.network("Server %s of type %s:%s has been started!", container.id(), container.group(),
                            container.type()));
                }
            });
        }).subscribe(Subjects.SERVER_REGISTER);
        connection.createDispatcher(msg -> {
            var container = ServerStatusContainer.deserialize(new String(msg.getData()));
            network.getServers().remove(container.id(), container.server());
            Cytosis.getOnlinePlayers().forEach(player -> {
                if (!player.isAdmin()) return;
                if (player.getPreference(CytosisPreferences.SERVER_ALERTS)) {
                    player.sendMessage(
                        Msg.network("Server %s of type %s:%s has been shut down!", container.id(), container.group(),
                            container.type()));
                }
            });
        }).subscribe(Subjects.SERVER_SHUTDOWN);
        connection.createDispatcher(msg -> {
            var container = ServerStatusContainer.deserialize(new String(msg.getData()));
            network.getServers().remove(container.id(), container.server());
        }).subscribe(Subjects.SERVER_SHUTDOWN_NOTIFY);
    }

    private void listenForPlayerServerChange() {
        connection.createDispatcher(msg -> {
            PlayerChangeServerContainer container = PlayerChangeServerContainer.deserialize(new String(msg.getData()));
            network.processPlayerServerChange(container);
        }).subscribe(Subjects.PLAYER_SERVER_CHANGE);
    }

    private void listenForChatMessage() {
        connection.createDispatcher(msg -> {
            String data = new String(msg.getData());
            ChatMessage message = ChatMessage.fromJson(data);
            ChatChannel channel = message.channel();
            ChatChannel chatChannel = message.channel();
            Component component = JSONComponentSerializer.json().deserialize(message.serializedMessage());

            if (channel == ChatChannel.PRIVATE_MESSAGE) {
                if (message.recipients() == null || message.recipients().isEmpty()) return;
                Cytosis.getOnlinePlayers().forEach(player -> {
                    if (message.recipients().contains(player.getUuid())) {
                        //todo: add permission to message people
                        if (player.getPreference(CytosisPreferences.CHAT_MESSAGE_PING)) {
                            player.playSound(
                                Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                        }
                        player.sendMessage(component);
                        chatManager.openPrivateMessage(player, message.sender());
                    }
                });
                return;
            }

            if (channel == ChatChannel.INTERNAL_MESSAGE) {
                if (message.recipients() == null || message.recipients().isEmpty()) return;
                for (UUID uuid : message.recipients()) {
                    Cytosis.getPlayer(uuid).ifPresent(player -> player.sendMessage(component));
                }
            }

            if (!chatChannel.isSupportsSelectiveRecipients()) {
                Cytosis.getOnlinePlayers().forEach(player -> {
                    if (player.canUseChannel(chatChannel) && !player.getPreference(
                        CytosisNamespaces.IGNORED_CHAT_CHANNELS).getForChannel(chatChannel)) {
                        if (player.getPreference(CytosisPreferences.CHAT_MESSAGE_PING)) {
                            player.playSound(
                                Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                        }
                        player.sendMessage(component);
                    }
                });
            }
        }).subscribe(Subjects.CHAT_MESSAGE);
    }

    private void listenForPlayerRankUpdates() {
        connection.createDispatcher(msg -> {
            PlayerRankUpdateContainer container = PlayerRankUpdateContainer.deserialize(msg.getData());
            Cytosis.getPlayer(container.player()).ifPresentOrElse(player -> {
                // they are on this server, so we need to update their cosmetics
                rankManager.changeRank(player, container.rank());
                Component badge;
                if (container.rank() != PlayerRank.DEFAULT) {
                    badge = container.rank().getPrefix().replaceText(builder -> builder.match(" ").replacement(""));
                } else {
                    badge = Component.text(PlayerRank.DEFAULT.name(), PlayerRank.DEFAULT.getTeamColor());
                }
                player.sendMessage(Msg.network("Your rank has been updated to ").append(badge).append(Msg.grey(".")));

            }, () -> {
                rankManager.changeRankSilently(container.player(), container.rank());
                network.updateCachedPlayerRank(container.player(), container.rank());
                databaseManager.setPlayerRank(container.player(), container.rank());
            });
        }).subscribe(Subjects.PLAYER_RANK_UPDATE);
    }

    /**
     * Queues a publishing until the nats connection is completed.
     *
     * @param channel The channel to publish on
     * @param data    The data to publish
     */
    public void publish(String channel, byte[] data) {
        if (connection != null) {
            connection.publish(channel, data);
            return;
        }

        publishQueue.add(new PublishContainer(channel, data));
    }

    public void sendFriendRequest(FriendRequest request) {
        request(Subjects.FRIEND_REQUEST, request.serialize().getBytes(StandardCharsets.UTF_8), (message, throwable) -> {
            if (Cytosis.getPlayer(request.sender()).isEmpty()) {
                return; // not online, don't care anymore.
            }
            CytosisPlayer p = Cytosis.getPlayer(request.sender()).get();
            if (throwable != null) {
                p.sendMessage(Msg.serverError("An error occurred whilst sending your friend request!"));
                Logger.error("Internal error whilst sending friend request", throwable);
            }
            FriendApiResponse response = FriendApiResponse.deserialize(new String(message.getData()));
            if (response.success()) {
                return; // it was successful, no need to tell anyone
            }

            String recipientName = network.getLifetimePlayers().getByKey(request.recipient());
            PlayerRank recipientRank = network.getCachedPlayerRanks().get(request.recipient());
            Component recipient = recipientRank.getPrefix().append(Component.text(recipientName));

            if (response.code().equalsIgnoreCase("ALREADY_SENT")) {
                p.sendMessage(Msg.whoops("You have already sent a friend request to ").append(recipient)
                    .append(Msg.mm("<gray>!")));
            } else {
                p.sendMessage(Msg.serverError("Failed to send your friend request to ").append(recipient)
                    .append(Msg.mm("<gray>! Error: " + response.message())));
                Logger.error(
                    "Failed to send " + request.sender() + "'s friend request to " + request.recipient() + "!. Error: "
                        + response.message() + " | Code: " + response.code());
            }
        });
    }

    public void request(String channel, byte[] data, BiConsumer<Message, Throwable> consumer) {
        if (connection != null) {
            connection.request(channel, data).whenComplete(consumer);
            return;
        }

        requestQueue.add(new RequestContainer(channel, data, consumer));
    }

    public void acceptFriendRequest(UUID requestId) {
        request(Subjects.FRIEND_ACCEPT_BY_ID, FriendResponse.create(requestId),
            (m, t) -> handleAccept(new String(m.getData()), t, null, null));
    }

    public void acceptFriendRequest(UUID sender, UUID recipient) {
        request(Subjects.FRIEND_ACCEPT, OrganicFriendResponse.create(sender, recipient),
            (m, t) -> handleAccept(new String(m.getData()), t, recipient, sender));
    }

    private void handleAccept(String response, @Nullable Throwable throwable, @Nullable UUID recipient,
        @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient)
                    .ifPresent(player -> player.sendMessage(Msg.serverError("Failed to process your friend request!")));
            }
            Logger.error("Internal error upon processing a friend acceptance.", throwable);
        }
        FriendApiResponse api = FriendApiResponse.deserialize(response);
        if (api.success()) return;

        String senderName = network.getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = network.getCachedPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (api.message().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.whoops("You don't have an active friend request from ").append(senderComp)
                    .append(Msg.mm("<gray>!"))));
        }

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.serverError("Failed to process accepting your friend request: " + api.message())));
        }
        Logger.info("Failed to accept friend request: " + api.code());
    }

    public void declineFriendRequest(UUID requestId) {
        request(Subjects.FRIEND_DECLINE_BY_ID, FriendResponse.create(requestId),
            (m, t) -> handleDecline(new String(m.getData()), t, null, null));
    }

    public void declineFriendRequest(UUID sender, UUID recipient) {
        request(Subjects.FRIEND_DECLINE, OrganicFriendResponse.create(sender, recipient),
            (m, t) -> handleDecline(new String(m.getData()), t, recipient, sender));
    }

    private void handleDecline(String response, @Nullable Throwable throwable, @Nullable UUID recipient,
        @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient).ifPresent(
                    player -> player.sendMessage(Msg.serverError("Failed to process declining your friend request!")));
            }
            Logger.error("Internal error upon proccessing a friend decline.", throwable);
        }
        FriendApiResponse api = FriendApiResponse.deserialize(response);

        String senderName = network.getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = network.getCachedPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (api.message().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.whoops("You don't have an active friend request from ").append(senderComp)
                    .append(Msg.mm("<gray>!"))));
        }

        if (api.success()) return;

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.serverError("Failed to process declining your friend request: " + api.message())));
        }
        Logger.info("Failed to accept friend request: " + api.code());
    }

    public void broadcastFriendRemoval(UUID sender, UUID recipient) {
        publish(Subjects.FRIEND_REMOVED, Tuple.of(sender, recipient).toString().getBytes());
    }

    public void fetchServers() {
        request(Subjects.SERVER_LIST, new byte[]{}, (m, t) -> {

            if (t != null) {
                Logger.error("failed to fetch active servers!", t);
                return;
            }

            try {
                List<ServerStatusContainer> containers = Cytosis.GSON.fromJson(new String(m.getData()),
                    Utils.SERVER_LIST);
                for (ServerStatusContainer container : containers) {
                    network.getServers().put(container.id(), container.server());
                    Logger.info("Loaded server '" + container.id() + "' from Cydian!");
                }
                Logger.info("Loaded " + containers.size() + " active servers from Cydian!");
            } catch (Exception e) {
                Logger.error("ERORR: ", e);
            }
        });
    }

    /**
     * Sends a message to Redis to kick a player.
     * <p>
     *
     * @param player    The player to kick, on this server
     * @param reason    The reason for kicking the player
     * @param component The kick message displayed
     */
    public void kickPlayer(Player player, KickReason reason, Component component) {
        kickPlayer(player.getUuid(), reason, component);
    }

    /**
     * Sends a message to Redis to kick a player.
     * <p>
     *
     * @param player    The player to kick, on another server
     * @param reason    The reason for kicking the player
     * @param component The kick message displayed
     */
    public void kickPlayer(UUID player, KickReason reason, Component component) {
        PlayerKickContainer container = new PlayerKickContainer(player, reason,
            JSONComponentSerializer.json().serialize(component));
        publish(Subjects.PLAYER_KICK, container.toString().getBytes());
    }

    /**
     * Sends a message to the redis server telling the proxies to move a player to a different server
     *
     * @param player The player to move
     * @param server the destination server
     */
    public void sendPlayerToServer(UUID player, CytonicServer server, @Nullable UUID instance) {
        request(Subjects.PLAYER_SEND,
            new SendPlayerToServerContainer(player, server.id(), instance).serialize().getBytes(),
            (message, throwable) -> {
                if (Cytosis.getPlayer(player).isEmpty()) return;
                Player p = Cytosis.getPlayer(player).get();
                if (throwable != null) {
                    p.sendMessage(Msg.serverError("An error occured whilst sending you to %s!", server.id()));
                }

                ServerSendReponse response = ServerSendReponse.parse(message.getData());

                if (!response.success()) {
                    p.sendMessage(
                        Msg.serverError("An error occured whilst sending you to %s! <red>(%s)</red>", server.id(),
                            response.message()));
                } else {
                    p.sendMessage(Msg.network("Sending you to %s!", server.id()));
                }
            });
    }

    public void sendPlayerToServer(UUID player, String serverID, @Nullable UUID instance) {
        request(Subjects.PLAYER_SEND,
            new SendPlayerToServerContainer(player, serverID, instance).serialize().getBytes(),
            (message, throwable) -> {
                if (Cytosis.getPlayer(player).isEmpty()) return;
                Player p = Cytosis.getPlayer(player).get();
                if (throwable != null) {
                    p.sendMessage(Msg.serverError("An error occured whilst sending you to %s!", serverID));
                }

                ServerSendReponse response = ServerSendReponse.parse(message.getData());

                if (!response.success()) {
                    p.sendMessage(
                        Msg.serverError("An error occured whilst sending you to %s! <red>(%s)</red>", serverID,
                            response.message()));
                } else {
                    p.sendMessage(Msg.network("Sending you to %s!", serverID));
                }
            });
    }

    public void sendPlayerRankUpdate(UUID uuid, PlayerRank rank) {
        publish(Subjects.PLAYER_RANK_UPDATE, new PlayerRankUpdateContainer(uuid, rank).serialize());
    }

    public void sendPlayerToGenericServer(UUID player, String group, String id, @Nullable String displayname) {
        request(Subjects.PLAYER_SEND_GENERIC, new SendToServerTypeContainer(player, group, id).serialize(),
            (message, throwable) -> {
                if (Cytosis.getPlayer(player).isEmpty()) return;
                Player p = Cytosis.getPlayer(player).get();
                if (throwable != null) {
                    p.sendMessage(Msg.serverError("An error occured whilst sending you to %s!",
                        displayname == null ? "the a server" : displayname));
                    Logger.error("An error occured whilst sending " + player + " to a generic " + group + ":" + id
                        + "! <red>(%s)</red>", throwable);
                }

                ServerSendReponse response = ServerSendReponse.parse(message.getData());

                if (!response.success()) {
                    p.sendMessage(Msg.serverError("An error occured whilst sending you to %s! <red>(%s)</red>",
                        displayname == null ? "the a server" : displayname, response.message()));
                } else {
                    p.sendMessage(
                        Msg.network("Sending you to %s!", displayname == null ? "the a server" : displayname));
                }
            });
    }

    public void sendChatMessage(ChatMessage chatMessage) {
        publish(Subjects.CHAT_MESSAGE, chatMessage.toJson().getBytes(StandardCharsets.UTF_8));
    }

    public void subscribe(String channel, Consumer<Message> consumer) {
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