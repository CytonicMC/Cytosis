package net.cytonic.cytosis.data.packet.publishers;

import java.util.Optional;
import java.util.UUID;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.packet.packets.friends.FriendApiRequestPacket;
import net.cytonic.cytosis.data.packet.packets.friends.FriendApiResponsePacket;
import net.cytonic.cytosis.data.packet.packets.friends.FriendIdPacket;
import net.cytonic.cytosis.data.packet.packets.friends.FriendPacket;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

@CytosisComponent(dependsOn = CytonicNetwork.class)
@NoArgsConstructor
public class FriendPacketsPublisher implements Bootstrappable {

    private CytonicNetwork network;

    @Override
    public void init() {
        network = Cytosis.get(CytonicNetwork.class);
    }

    public void sendFriendRequest(FriendApiRequestPacket packet) {
        Optional<CytosisPlayer> playerOptional = Cytosis.getPlayer(packet.getSender());
        if (playerOptional.isEmpty()) {
            packet.request();
            return;
        }
        packet.request((response, throwable) -> {
            CytosisPlayer player = playerOptional.get();

            if (throwable != null) {
                player.sendMessage(Msg.serverError("An error occurred whilst sending your friend request!"));
            }
            if (response.isSuccess()) {
                return;
            }

            String recipientName = network.getLifetimePlayers().getByKey(packet.getRecipient());
            PlayerRank recipientRank = network.getCachedPlayerRanks().get(packet.getRecipient());
            Component recipient = recipientRank.getPrefix().append(Component.text(recipientName));

            if (response.getCode().equals("ALREADY_SENT")) {
                player.sendMessage(Msg.whoops("You have already sent a friend request to ").append(recipient)
                    .append(Msg.mm("<gray>!")));
                return;
            }

            player.sendMessage(Msg.serverError("Failed to send your friend request to ").append(recipient)
                .append(Msg.mm("<gray>! Error: " + response.getMessage())));

            Logger.error(
                "Failed to send " + packet.getSender() + "'s friend request to " + packet.getRecipient() + "!. Error: "
                    + response.getMessage() + " | Code: " + response.getCode());

        });
    }

    public void sendAcceptFriendRequest(UUID requestId) {
        new FriendIdPacket(requestId).request(Subjects.FRIEND_ACCEPT_BY_ID,
            (response, throwable) -> handleAccept(response, throwable, null, null));
    }

    public void sendAcceptFriendRequest(UUID sender, UUID recipient) {
        new FriendPacket(sender, recipient).request(Subjects.FRIEND_ACCEPT,
            (response, throwable) -> handleAccept(response, throwable, recipient, sender));
    }

    private void handleAccept(FriendApiResponsePacket response, @Nullable Throwable throwable,
        @Nullable UUID recipient, @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient)
                    .ifPresent(player -> player.sendMessage(Msg.serverError("Failed to process your friend request!")));
            }
            Logger.error("Internal error upon processing a friend acceptance.", throwable);
        }
        if (response.isSuccess()) {
            return;
        }

        String senderName = network.getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = network.getCachedPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (response.getMessage().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.whoops("You don't have an active friend request from ").append(senderComp)
                    .append(Msg.mm("<gray>!"))));
        }

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.serverError("Failed to process accepting your friend request: " + response.getMessage())));
        }
        Logger.info("Failed to accept friend request: " + response.getCode());
    }

    public void sendDeclineFriendRequest(UUID requestId) {
        new FriendIdPacket(requestId).request(Subjects.FRIEND_DECLINE_BY_ID,
            (response, throwable) -> handleDecline(response, throwable, null, null));
    }

    public void sendDeclineFriendRequest(UUID sender, UUID recipient) {
        new FriendPacket(sender, recipient).request(Subjects.FRIEND_DECLINE,
            (response, throwable) -> handleDecline(response, throwable, recipient, sender));
    }

    private void handleDecline(FriendApiResponsePacket response, @Nullable Throwable throwable,
        @Nullable UUID recipient, @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient).ifPresent(
                    player -> player.sendMessage(Msg.serverError("Failed to process declining your friend request!")));
            }
            Logger.error("Internal error upon processing a friend decline.", throwable);
        }

        String senderName = network.getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = network.getCachedPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (response.getMessage().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.whoops("You don't have an active friend request from ").append(senderComp)
                    .append(Msg.mm("<gray>!"))));
        }

        if (response.isSuccess()) {
            return;
        }

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.serverError("Failed to process declining your friend request: " + response.getMessage())));
        }
        Logger.info("Failed to accept friend request: " + response.getCode());
    }

    public void sendFriendRemove(UUID sender, UUID recipient) {
        new FriendPacket(sender, recipient).request(Subjects.FRIEND_REMOVE);
    }
}
