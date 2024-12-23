package net.cytonic.cytosis.messaging.nats;

import io.nats.client.*;
import lombok.SneakyThrows;
import net.cytonic.containers.PlayerLoginLogoutContainer;
import net.cytonic.containers.ServerStatusContainer;
import net.cytonic.containers.friends.FriendApiResponse;
import net.cytonic.containers.friends.FriendRequest;
import net.cytonic.containers.friends.FriendResponse;
import net.cytonic.containers.friends.OrganicFriendResponse;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Utils;
import net.cytonic.enums.PlayerRank;
import net.cytonic.objects.CytonicServer;
import net.cytonic.objects.Tuple;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.nats.client.ConnectionListener.Events.*;
import static net.cytonic.utils.MiniMessageTemplate.MM;

public class NatsManager {


    Connection connection;
    Subscription healthCheck;
    private boolean started = false;

    @SneakyThrows
    public void setup() {
        ConnectionListener connectionListener = (conn, type) -> {

            Objects.requireNonNull(type);

            if (type == CONNECTED || type == RESUBSCRIBED || type == RECONNECTED) {
                connection = conn;
                Logger.info("Connected asynchronously to NATS server!");
                startHealthCheck();
                if (!started) {
                    started = true;
                    listenForFriends();
                    listenForPlayerLoginLogout();
                    listenForFriendRequestNotification();
                    listenForFriendRemoval();
                    listenForServerStatus();
                }
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
        byte[] data = new ServerStatusContainer("TYPE_HERE", Utils.getServerIP(), Cytosis.getRawID(), CytosisSettings.SERVER_PORT, Instant.now()).serialize().getBytes();
        Thread.ofVirtual().name("NATS Startup Publisher").start(() -> {
                    try {
                        Logger.info("Registering server with Cydian!");
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
        final Component line = MM."<st><dark_aqua>                                                                                 ";

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
                        player.sendMessage(MM."<aqua>Your friend request from ".append(sender).append(MM."<aqua> has expired!"));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(request.sender())) {
                        player.sendMessage(line);
                        player.sendMessage(MM."<aqua>Your friend request to ".append(target).append(MM."<aqua> has expired!"));
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
                        player.sendMessage(MM."<aqua>You accepted ".append(sender).append(MM."<aqua>'s friend request!"));
                        player.sendMessage(line);

                        // this server gets the authority to add them as a friend in the database
                        Cytosis.getFriendManager().addFriend(request.sender(), request.recipient());
                    } else if (player.getUuid().equals(request.sender())) {
                        player.sendMessage(line);
                        player.sendMessage(target.append(MM."<aqua> accepted your friend request!"));
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
                        player.sendMessage(MM."<red>You declined ".append(sender).append(MM."<red>'s friend request!"));
                        player.sendMessage(line);
                    } else if (player.getUuid().equals(request.sender())) {
                        player.sendMessage(line);
                        player.sendMessage(target.append(MM."<red> declined your friend request!"));
                        player.sendMessage(line);
                    }
                }
                msg.ack();
            });
        });
    }

    public void sendFriendRequest(FriendRequest request) {
        Thread.ofVirtual().name("NATS Friend Send").start(() -> connection.request(Subjects.FRIEND_REQUEST, request.serialize().getBytes(StandardCharsets.UTF_8)).whenComplete((message, throwable) -> {
            if (Cytosis.getPlayer(request.sender()).isEmpty()) return; // not online, don't care anymore.
            CytosisPlayer p = Cytosis.getPlayer(request.sender()).get();
            if (throwable != null) {
                p.sendMessage(MM."<red><b>SERVER ERROR!</b></red><gray> An error occured whilst sending your friend request!");
                Logger.error("Internal error whilst sending friend request", throwable);
            }
            FriendApiResponse response = FriendApiResponse.deserialize(new String(message.getData()));
            if (response.success()) return; // it was successful, no need to tell anyone

            String recipientName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(request.recipient());
            PlayerRank recipientRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(request.recipient());
            Component recipient = recipientRank.getPrefix().append(Component.text(recipientName));

            if (response.code().equalsIgnoreCase("ALREADY_SENT")) {
                p.sendMessage(MM."<red><b>WHOOPS!</b></red><gray> You have already sent a friend request to ".append(recipient).append(MM."<gray>!"));
            } else {
                p.sendMessage(MM."<red><b>SERVER EEROR!</b></red><gray> Failed to send your friend request to ".append(recipient).append(MM."<gray>! Error: \{response.message()}"));
                Logger.error(STR."Failed to send \{request.sender()}'s friend request to \{request.recipient()}!. Error: \{response.message()} | Code: \{response.code()}");
            }
        }));
    }

    public void acceptFriendRequest(UUID requestId) {
        Thread.ofVirtual().name("NATS Friend Accept").start(() ->
                connection.request(Subjects.FRIEND_ACCEPT_BY_ID, FriendResponse.create(requestId)).whenComplete((m, t) ->
                        handleAccept(new String(m.getData()), t, null, null)));
    }

    public void declineFriendRequest(UUID requestId) {
        Thread.ofVirtual().name("NATS Friend Decline").start(() ->
                connection.request(Subjects.FRIEND_DECLINE_BY_ID, FriendResponse.create(requestId)).whenComplete((m, t) ->
                        handleDecline(new String(m.getData()), t, null, null)));
    }

    public void acceptFriendRequest(UUID sender, UUID recipient) {
        Thread.ofVirtual().name("NATS Friend Accept").start(() ->
                connection.request(Subjects.FRIEND_ACCEPT, OrganicFriendResponse.create(sender, recipient)).whenComplete((m, t) ->
                        handleAccept(new String(m.getData()), t, recipient, sender)));
    }

    public void declineFriendRequest(UUID sender, UUID recipient) {
        Thread.ofVirtual().name("NATS Friend Decline").start(() ->
                connection.request(Subjects.FRIEND_DECLINE, OrganicFriendResponse.create(sender, recipient)).whenComplete((m, t) ->
                        handleDecline(new String(m.getData()), t, recipient, sender)));
    }

    private void handleAccept(String response, @Nullable Throwable throwable, @Nullable UUID recipient, @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(MM."<red><b>SERVER ERROR</b></red> <gray> Failed to process your friend request!"));
            }
            Logger.error("Internal error upon proccessing a friend acceptance.", throwable);
        }
        FriendApiResponse api = FriendApiResponse.deserialize(response);
        if (api.success()) return;

        String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (api.message().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You don't have an active friend request from ".append(senderComp).append(MM."<gray>!")));
        }

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(MM."<red><b>SERVER ERROR</b></red> <gray> Failed to process accepting your friend request: \{api.message()}"));
        }
        Logger.info(STR."Failed to accept friend request: \{api.code()}");
    }

    private void handleDecline(String response, @Nullable Throwable throwable, @Nullable UUID recipient, @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(MM."<red><b>SERVER ERROR</b></red> <gray> Failed to process declining your friend request!"));
            }
            Logger.error("Internal error upon proccessing a friend decline.", throwable);
        }
        FriendApiResponse api = FriendApiResponse.deserialize(response);

        String senderName = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = Cytosis.getCytonicNetwork().getPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (api.message().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(MM."<red><b>ERROR!</b></red> <gray>You don't have an active friend request from ".append(senderComp).append(MM."<gray>!")));
        }

        if (api.success()) return;

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(MM."<red><b>SERVER ERROR</b></red> <gray> Failed to process declining your friend request: \{api.message()}"));
        }
        Logger.info(STR."Failed to accept friend request: \{api.code()}");
    }

    public void listenForPlayerLoginLogout() {
        //todo: probably tuck the friend notifcations in here
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
        }).subscribe(Subjects.PLAYER_LEAVE));
    }

    public void listenForFriendRequestNotification() {
        final Component line = MM."<st><dark_aqua>                                                                                 ";

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
                    player.sendMessage(sender.append(MM."<aqua> sent you a friend request! You have 5 minutes to accept it! ").append(MM."<green><b><click:run_command:/friend accept \{request.sender()}>[Accept]</click></b></green> <red><b><click:run_command:/friend decline \{request.sender()}>[Decline]</click></b></red>"));
                    player.sendMessage(line);
                } else if (request.sender().equals(player.getUuid())) {
                    player.sendMessage(line);
                    player.sendMessage(MM."<aqua>You send a friend request to ".append(target).append(MM."<aqua>! They have 5 minutes to accept it!"));
                    player.sendMessage(line);
                }
            }
        }).subscribe(Subjects.FRIEND_REQUEST_NOTIFY));
    }

    public void broadcastFriendRemoval(UUID sender, UUID recipient) {
        connection.publish(Subjects.FRIEND_REMOVED, Tuple.of(sender, recipient).toString().getBytes());
    }

    public void listenForFriendRemoval() {
        final Component line = MM."<st><dark_aqua>                                                                                 ";
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
                    player.sendMessage(sender.append(MM."<aqua> removed you from their friend list!"));
                    player.sendMessage(line);
                } else if (player.getUuid().equals(tuple.getFirst())) {
                    player.sendMessage(line);
                    player.sendMessage(MM."<aqua>You removed ".append(target).append(MM." from your friend list!"));
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
    }

    public void fetchServers() {
        Thread.ofVirtual().name("NATS Server Fetcher").start(() ->
                connection.request(Subjects.SERVER_LIST, new byte[]{}).whenComplete((m, t) -> {

                    if (t != null) {
                        Logger.error("failed to fetch active servers!", t);
                        return;
                    }

                    try {
                        Logger.debug(new String(m.getData()));
                        List<ServerStatusContainer> containers = Cytosis.GSON.fromJson(new String(m.getData()), Utils.SERVER_LIST);
                        for (ServerStatusContainer container : containers) {
                            Cytosis.getCytonicNetwork().getServers().put(container.id(), container.server());
                            Logger.info(STR."Loaded server '\{container.id()}' from Cydian!");
                        }
                        Logger.info(STR."Loaded \{containers.size()} active servers from Cydian!");
                    } catch (Exception e) {
                        Logger.error("ERORR: ", e);
                    }
                }));
    }
}