package net.cytonic.cytosis.messaging.nats;


import io.nats.client.*;
import lombok.SneakyThrows;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.containers.PlayerKickContainer;
import net.cytonic.cytosis.data.containers.PlayerLoginLogoutContainer;
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
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.nats.client.ConnectionListener.Events.*;

public class NatsManager {


    Connection connection;
    Subscription healthCheck;
    private boolean started = false;

    private final ConcurrentLinkedDeque<PublishContainer> publishQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<RequestContainer> requestQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<SubscribeContainer> subscribeQueue = new ConcurrentLinkedDeque<>();

    @SneakyThrows
    public void setup() {
        ConnectionListener connectionListener = (conn, type) -> {

            Objects.requireNonNull(type);

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
                    listenForFriends();
                    listenForPlayerLoginLogout();
                    listenForFriendRequestNotification();
                    listenForFriendRemoval();
                    listenForServerStatus();
                    listenForPlayerServerChange();
                    listenForChatMessage();
                }
            } else {
                Logger.info("Disconnected from NATS server!");
                connection = null;
            }
        };

        Options options = Options.builder()
                .server("nats://" + CytosisSettings.NATS_USERNAME + ":" + CytosisSettings.NATS_PASSWORD + "@" + CytosisSettings.NATS_HOSTNAME + ":" + CytosisSettings.NATS_PORT)
                .connectionListener(connectionListener)
                .errorListener(new ErrorListener() {
                    @Override
                    public void errorOccurred(Connection conn, String error) {
                        Logger.error("An error occured in a NATS action: " + error + " in connection " + conn.getServerInfo().getClientId());
                    }
                })
                .build();
        Nats.connectAsynchronously(options, true);
    }

    @SneakyThrows // don't care about the error on shutdown
    public void shutdown() {
        sendShutdown();
        connection.close();
    }

    public void sendStartup() {
        byte[] data = new ServerStatusContainer(Cytosis.getServerGroup().type(), Utils.getServerIP(), Cytosis.getRawID(), CytosisSettings.SERVER_PORT, Instant.now(), Cytosis.getServerGroup().group()).serialize().getBytes();
        Thread.ofVirtual().name("NATS Startup Publisher").start(() -> {
                    try {
                        Logger.info("Registering server with Cydian!");
                        publish(Subjects.SERVER_REGISTER, data);
                    } catch (Exception e) {
                        Logger.error("Failed to send STARTUP", e);
                    }
                }
        );
    }

    public void sendShutdown() {
        byte[] data = new ServerStatusContainer(Cytosis.getServerGroup().type(), Utils.getServerIP(), Cytosis.getRawID(), CytosisSettings.SERVER_PORT, Instant.now(), Cytosis.getServerGroup().group()).serialize().getBytes();
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

    public void listenForFriends() {
        final Component line = Msg.mm("<st><dark_aqua>                                                                                 ");

        Thread.ofVirtual().name("NATS Friend Expiry Worker").start(() -> {
            Dispatcher dispatcher = connection.createDispatcher();
            dispatcher.subscribe(Subjects.FRIEND_EXPIRE_NOTIFY, msg -> {

                FriendRequest request = FriendRequest.deserialize(new String(msg.getData()));

                String targetName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.recipient());
                String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.sender());
                PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.recipient());
                PlayerRank senderRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.sender());


                Component target = targetRank.getPrefix().append(Component.text(targetName));
                Component sender = senderRank.getPrefix().append(Component.text(senderName));

                for (Player player : Cytosis.getOnlinePlayers()) {
                    if (player.getUuid().equals(request.recipient())) {
                        player.sendMessage(line);
                        player.sendMessage(Msg.aqua("Your friend request from ").append(sender).append(Msg.aqua(" has expired!")));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(request.sender())) {
                        player.sendMessage(line);
                        player.sendMessage(Msg.aqua("Your friend request to ").append(target).append(Msg.aqua(" has expired!")));
                        player.sendMessage(line);
                    }
                }
                msg.ack();
            });
        });
        Thread.ofVirtual().name("NATS Friend Accept Worker").start(() -> {
            Dispatcher dispatcher = connection.createDispatcher();
            dispatcher.subscribe(Subjects.FRIEND_ACCEPTANCE_NOTIFY, msg -> {

                FriendRequest request = FriendRequest.deserialize(new String(msg.getData()));

                String targetName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.recipient());
                String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.sender());
                PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.recipient());
                PlayerRank senderRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.sender());


                Component target = targetRank.getPrefix().append(Component.text(targetName));
                Component sender = senderRank.getPrefix().append(Component.text(senderName));

                for (Player player : Cytosis.getOnlinePlayers()) {
                    if (player.getUuid().equals(request.recipient())) {
                        player.sendMessage(line);
                        player.sendMessage(Msg.mm("<aqua>You accepted ").append(sender).append(Msg.mm("<aqua>'s friend request!")));
                        player.sendMessage(line);

                        // this server gets the authority to add them as a friend in the database
                        Cytosis.getFriendManager().addFriend(request.sender(), request.recipient());
                    } else if (player.getUuid().equals(request.sender())) {
                        player.sendMessage(line);
                        player.sendMessage(target.append(Msg.mm("<aqua> accepted your friend request!")));
                        player.sendMessage(line);
                    }
                }
                msg.ack();
            });
        });
        Thread.ofVirtual().name("NATS Friend Decline Worker").start(() -> {
            Dispatcher dispatcher = connection.createDispatcher();
            dispatcher.subscribe(Subjects.FRIEND_DECLINATION_NOTIFY, msg -> {

                FriendRequest request = FriendRequest.deserialize(new String(msg.getData()));

                String targetName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.recipient());
                String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.sender());
                PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.recipient());
                PlayerRank senderRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.sender());


                Component target = targetRank.getPrefix().append(Component.text(targetName));
                Component sender = senderRank.getPrefix().append(Component.text(senderName));

                for (Player player : Cytosis.getOnlinePlayers()) {
                    if (player.getUuid().equals(request.recipient())) {
                        player.sendMessage(line);
                        player.sendMessage(Msg.mm("<red>You declined ").append(sender).append(Msg.mm("<red>'s friend request!")));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(request.sender())) {
                        player.sendMessage(line);
                        player.sendMessage(target.append(Msg.mm("<red> declined your friend request!")));
                        player.sendMessage(line);
                    }
                }
                msg.ack();
            });
        });
    }

    public void sendFriendRequest(FriendRequest request) {
        Thread.ofVirtual().name("NATS Friend Send").start(() -> request(Subjects.FRIEND_REQUEST, request.serialize().getBytes(StandardCharsets.UTF_8), (message, throwable) -> {
            if (Cytosis.getPlayer(request.sender()).isEmpty()) return; // not online, don't care anymore.
            CytosisPlayer p = Cytosis.getPlayer(request.sender()).get();
            if (throwable != null) {
                p.sendMessage(Msg.serverError("An error occured whilst sending your friend request!"));
                Logger.error("Internal error whilst sending friend request", throwable);
            }
            FriendApiResponse response = FriendApiResponse.deserialize(new String(message.getData()));
            if (response.success()) return; // it was successful, no need to tell anyone

            String recipientName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.recipient());
            PlayerRank recipientRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.recipient());
            Component recipient = recipientRank.getPrefix().append(Component.text(recipientName));

            if (response.code().equalsIgnoreCase("ALREADY_SENT")) {
                p.sendMessage(Msg.whoops("You have already sent a friend request to ").append(recipient).append(Msg.mm("<gray>!")));
            } else {
                p.sendMessage(Msg.serverError("Failed to send your friend request to ").append(recipient).append(Msg.mm("<gray>! Error: " + response.message())));
                Logger.error("Failed to send " + request.sender() + "'s friend request to " + request.recipient() + "!. Error: " + response.message() + " | Code: " + response.code());
            }
        }));
    }

    public void acceptFriendRequest(UUID requestId) {
        Thread.ofVirtual().name("NATS Friend Accept").start(() ->
                request(Subjects.FRIEND_ACCEPT_BY_ID, FriendResponse.create(requestId), (m, t) ->
                        handleAccept(new String(m.getData()), t, null, null)));
    }

    public void declineFriendRequest(UUID requestId) {
        Thread.ofVirtual().name("NATS Friend Decline").start(() ->
                request(Subjects.FRIEND_DECLINE_BY_ID, FriendResponse.create(requestId), (m, t) ->
                        handleDecline(new String(m.getData()), t, null, null)));
    }

    public void acceptFriendRequest(UUID sender, UUID recipient) {
        Thread.ofVirtual().name("NATS Friend Accept").start(() ->
                request(Subjects.FRIEND_ACCEPT, OrganicFriendResponse.create(sender, recipient), (m, t) ->
                        handleAccept(new String(m.getData()), t, recipient, sender)));
    }

    public void declineFriendRequest(UUID sender, UUID recipient) {
        Thread.ofVirtual().name("NATS Friend Decline").start(() ->
                request(Subjects.FRIEND_DECLINE, OrganicFriendResponse.create(sender, recipient), (m, t) ->
                        handleDecline(new String(m.getData()), t, recipient, sender)));
    }

    private void handleAccept(String response, @Nullable Throwable throwable, @Nullable UUID recipient, @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(Msg.serverError("Failed to process your friend request!")));
            }
            Logger.error("Internal error upon proccessing a friend acceptance.", throwable);
        }
        FriendApiResponse api = FriendApiResponse.deserialize(response);
        if (api.success()) return;

        String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (api.message().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(Msg.whoops("You don't have an active friend request from ").append(senderComp).append(Msg.mm("<gray>!"))));
        }

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(Msg.serverError("Failed to process accepting your friend request: " + api.message())));
        }
        Logger.info("Failed to accept friend request: " + api.code());
    }

    private void handleDecline(String response, @Nullable Throwable throwable, @Nullable UUID recipient, @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(Msg.serverError("Failed to process declining your friend request!")));
            }
            Logger.error("Internal error upon proccessing a friend decline.", throwable);
        }
        FriendApiResponse api = FriendApiResponse.deserialize(response);

        String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (api.message().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(Msg.whoops("You don't have an active friend request from ").append(senderComp).append(Msg.mm("<gray>!"))));
        }

        if (api.success()) return;

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(Msg.serverError("Failed to process declining your friend request: " + api.message())));
        }
        Logger.info("Failed to accept friend request: " + api.code());
    }

    public void listenForPlayerLoginLogout() {
        Thread.ofVirtual().name("NATS Player Join").start(() -> connection.createDispatcher(msg -> {
            var container = PlayerLoginLogoutContainer.deserialize(new String(msg.getData()));
            Cytosis.getCytonicNetwork().addPlayer(container.username(), container.uuid());
            Cytosis.getPreferenceManager().loadPlayerPreferences(container.uuid());
            Cytosis.getFriendManager().sendLoginMessage(container.uuid());
        }).subscribe(Subjects.PLAYER_JOIN));

        Thread.ofVirtual().name("NATS Player Leave").start(() -> connection.createDispatcher(msg -> {
            var container = PlayerLoginLogoutContainer.deserialize(new String(msg.getData()));
            Cytosis.getCytonicNetwork().removePlayer(container.username(), container.uuid());
            Cytosis.getPreferenceManager().unloadPlayerPreferences(container.uuid());
            Cytosis.getFriendManager().sendLogoutMessage(container.uuid());
            Cytosis.getRankManager().removePlayer(container.uuid());
        }).subscribe(Subjects.PLAYER_LEAVE));
    }

    public void listenForFriendRequestNotification() {
        final Component line = Msg.mm("<st><dark_aqua>                                                                                 ");

        Thread.ofVirtual().name("NATS Friend Accept").start(() -> connection.createDispatcher(msg -> {
            FriendRequest request = FriendRequest.deserialize(new String(msg.getData()));

            String targetName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.recipient());
            String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.sender());
            PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.recipient());
            PlayerRank senderRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.sender());
            Component target = targetRank.getPrefix().append(Component.text(targetName));
            Component sender = senderRank.getPrefix().append(Component.text(senderName));

            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                if (request.recipient().equals(player.getUuid())) {
                    player.sendMessage(line);
                    player.sendMessage(sender.append(Msg.mm("<aqua> sent you a friend request! You have 5 minutes to accept it! ").append(Msg.mm("<green><b><click:run_command:/friend accept " + request.sender() + ">[Accept]</click></b></green> <red><b><click:run_command:/friend decline " + request.sender() + ">[Decline]</click></b></red>"))));
                    player.sendMessage(line);
                } else if (request.sender().equals(player.getUuid())) {
                    player.sendMessage(line);
                    player.sendMessage(Msg.mm("<aqua>You send a friend request to ").append(target).append(Msg.mm("<aqua>! They have 5 minutes to accept it!")));
                    player.sendMessage(line);
                }
            }
        }).subscribe(Subjects.FRIEND_REQUEST_NOTIFY));
    }

    public void broadcastFriendRemoval(UUID sender, UUID recipient) {
        publish(Subjects.FRIEND_REMOVED, Tuple.of(sender, recipient).toString().getBytes());
    }

    public void listenForFriendRemoval() {
        final Component line = Msg.mm("<st><dark_aqua>                                                                                 ");
        connection.createDispatcher(msg -> {
            Tuple<UUID, UUID> tuple = Tuple.deserialize(new String(msg.getData()), Tuple.UUID_TYPE);

            String targetName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(tuple.getSecond());
            String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(tuple.getFirst());
            PlayerRank targetRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(tuple.getSecond());
            PlayerRank senderRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(tuple.getFirst());


            Component target = targetRank.getPrefix().append(Component.text(targetName));
            Component sender = senderRank.getPrefix().append(Component.text(senderName));

            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                if (player.getUuid().equals(tuple.getSecond())) {
                    player.sendMessage(line);
                    player.sendMessage(sender.append(Msg.mm("<aqua> removed you from their friend list!")));
                    player.sendMessage(line);
                } else if (player.getUuid().equals(tuple.getFirst())) {
                    player.sendMessage(line);
                    player.sendMessage(Msg.mm("<aqua>You removed ").append(target).append(Msg.mm(" from your friend list!")));
                    player.sendMessage(line);
                }
            }

        }).subscribe(Subjects.FRIEND_REMOVED);
    }

    public void listenForServerStatus() {
        Thread.ofVirtual().name("NATS Server Start").start(() -> connection.createDispatcher(msg -> {
            var container = ServerStatusContainer.deserialize(new String(msg.getData()));
            Cytosis.getCytonicNetwork().getServers().put(container.id(), new CytonicServer(container.ip(), container.id(), container.port()));
            Logger.info("Registered server: " + container.id());
        }).subscribe(Subjects.SERVER_REGISTER));

        Thread.ofVirtual().name("NATS Server Stop").start(() -> connection.createDispatcher(msg -> {
            var container = ServerStatusContainer.deserialize(new String(msg.getData()));
            Cytosis.getCytonicNetwork().getServers().remove(container.id(), new CytonicServer(container.ip(), container.id(), container.port()));
        }).subscribe(Subjects.SERVER_SHUTDOWN));

        Thread.ofVirtual().name("NATS Server Proxy Shutdown").start(() -> connection.createDispatcher(msg -> {
            var container = ServerStatusContainer.deserialize(new String(msg.getData()));
            Cytosis.getCytonicNetwork().getServers().remove(container.id(), new CytonicServer(container.ip(), container.id(), container.port()));
        }).subscribe(Subjects.SERVER_SHUTDOWN_NOTIFY));
    }

    public void fetchServers() {
        Thread.ofVirtual().name("NATS Server Fetcher").start(() ->
                request(Subjects.SERVER_LIST, new byte[]{}, (m, t) -> {

                    if (t != null) {
                        Logger.error("failed to fetch active servers!", t);
                        return;
                    }

                    try {
                        List<ServerStatusContainer> containers = Cytosis.GSON.fromJson(new String(m.getData()), Utils.SERVER_LIST);
                        for (ServerStatusContainer container : containers) {
                            Cytosis.getCytonicNetwork().getServers().put(container.id(), container.server());
                            Logger.info("Loaded server '" + container.id() + "' from Cydian!");
                        }
                        Logger.info("Loaded " + containers.size() + " active servers from Cydian!");
                    } catch (Exception e) {
                        Logger.error("ERORR: ", e);
                    }
                }));
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
        PlayerKickContainer container = new PlayerKickContainer(player, reason, JSONComponentSerializer.json().serialize(component));
        Thread.ofVirtual().name("NATS player kicker").start(() -> publish(Subjects.PLAYER_KICK, container.toString().getBytes()));
    }

    /**
     * Sends a message to the redis server telling the proxies to move a player to a different server
     *
     * @param player The player to move
     * @param server the destination server
     */
    public void sendPlayerToServer(UUID player, CytonicServer server, @Nullable UUID instance) {
        Thread.ofVirtual().name("NATS Player Sender").start(() -> request(Subjects.PLAYER_SEND, new SendPlayerToServerContainer(player, server.id(), instance).serialize().getBytes(), (message, throwable) -> {
            if (Cytosis.getPlayer(player).isEmpty()) return;
            Player p = Cytosis.getPlayer(player).get();
            if (throwable != null) {
                p.sendMessage(Msg.serverError("An error occured whilst sending you to %s!", server.id()));
            }

            ServerSendReponse reponse = ServerSendReponse.parse(message.getData());

            if (!reponse.success()) {
                p.sendMessage(Msg.serverError("An error occured whilst sending you to %s! <red>(%s)</red>", server.id(), reponse.message()));
            } else {
                p.sendMessage(Msg.network("Sending you to %s!", server.id()));
            }
        }));
    }

    public void sendPlayerToServer(UUID player, String serverID, @Nullable UUID instance) {
        Thread.ofVirtual().name("NATS Player Sender").start(() -> request(Subjects.PLAYER_SEND, new SendPlayerToServerContainer(player, serverID, instance).serialize().getBytes(), (message, throwable) -> {
            if (Cytosis.getPlayer(player).isEmpty()) return;
            Player p = Cytosis.getPlayer(player).get();
            if (throwable != null) {
                p.sendMessage(Msg.serverError("An error occured whilst sending you to %s!", serverID));
            }

            ServerSendReponse reponse = ServerSendReponse.parse(message.getData());

            if (!reponse.success()) {
                p.sendMessage(Msg.serverError("An error occured whilst sending you to %s! <red>(%s)</red>", serverID, reponse.message()));
            } else {
                p.sendMessage(Msg.network("Sending you to %s!", serverID));
            }
        }));
    }

    public void sendPlayerToServer(UUID player, String group, String id, @Nullable String displayname) {
        Thread.ofVirtual().name("NATS Player Sender").start(() -> request(Subjects.PLAYER_SEND_GENERIC, new SendToServerTypeContainer(player, group, id).serialize(), (message, throwable) -> {
            if (Cytosis.getPlayer(player).isEmpty()) return;
            Player p = Cytosis.getPlayer(player).get();
            if (throwable != null) {
                p.sendMessage(Msg.serverError("An error occured whilst sending you to %s!", displayname == null ? "the a server" : displayname));
                Logger.error("An error occured whilst sending " + player + " to a generic " + group + ":" + id + "! <red>(%s)</red>", throwable);
            }

            ServerSendReponse reponse = ServerSendReponse.parse(message.getData());

            Logger.debug("Sent " + reponse.message() + " to " + player);
            if (!reponse.success()) {
                p.sendMessage(Msg.serverError("An error occured whilst sending you to %s! <red>(%s)</red>", displayname == null ? "the a server" : displayname, reponse.message()));
            } else {
                p.sendMessage(Msg.network("Sending you to %s!", displayname == null ? "the a server" : displayname));
            }
        }));
    }

    public void listenForPlayerServerChange() {
        Thread.ofVirtual().name("NATS Player Server Change").start(() -> connection.createDispatcher(msg -> {
            PlayerChangeServerContainer container = PlayerChangeServerContainer.deserialize(new String(msg.getData()));
            Cytosis.getCytonicNetwork().processPlayerServerChange(container);
        }).subscribe(Subjects.PLAYER_SERVER_CHANGE));
    }

    public void sendChatMessage(ChatMessage chatMessage) {
        Thread.ofVirtual().name("NATS Chat Message Sender").start(() -> {
            try {
                publish(Subjects.CHAT_MESSAGE, chatMessage.toJson().getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                Logger.error("Failed to send chat message", e);
            }
        });
    }

    public void listenForChatMessage() {
        Thread.ofVirtual().name("NATS Chat Message Reciever").start(() -> {
            try {
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
                                player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                                player.sendMessage(component);
                                Cytosis.getChatManager().openPrivateMessage(player, message.sender());
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

                    // these channels don't support selective recipients
                    if (chatChannel == ChatChannel.ADMIN || chatChannel == ChatChannel.MOD || chatChannel == ChatChannel.STAFF) {
                        Cytosis.getOnlinePlayers().forEach(player -> {
                            if (player.canUseChannel(chatChannel) && !player.getPreference(CytosisNamespaces.IGNORED_CHAT_CHANNELS).getForChannel(chatChannel)) {
                                player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, .7f, 1.0F));
                                player.sendMessage(component);
                            }
                        });
                    }
                }).subscribe(Subjects.CHAT_MESSAGE);
            } catch (Exception e) {
                Logger.error("Failed to receive chat message", e);
            }
        });
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

    public void request(String channel, byte[] data, BiConsumer<Message, Throwable> consumer) {
        if (connection != null) {
            connection.request(channel, data).whenComplete(consumer);
            return;
        }

        requestQueue.add(new RequestContainer(channel, data, consumer));
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