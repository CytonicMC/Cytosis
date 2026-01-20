package net.cytonic.cytosis.protocol.publishers;

import java.util.Optional;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.impl.notifyPackets.FriendNotifyPacket;
import net.cytonic.protocol.impl.objects.FriendApiIdProtocolObject;
import net.cytonic.protocol.impl.objects.FriendApiProtocolObject;

@CytosisComponent(dependsOn = CytonicNetwork.class)
public class FriendPacketsPublisher {

    private final CytonicNetwork network = Cytosis.get(CytonicNetwork.class);

    public void sendFriendRequest(FriendApiProtocolObject.Packet packet) {
        Optional<CytosisPlayer> playerOptional = Cytosis.getPlayer(packet.sender());
        if (playerOptional.isEmpty()) {
            packet.publish();
            return;
        }
        packet.request((response, throwable) -> {
            CytosisPlayer player = playerOptional.get();

            if (throwable != null) {
                player.sendMessage(Msg.serverError("An error occurred whilst sending your friend request!"));
            }
            if (response.success()) {
                return;
            }

            String recipientName = network.getLifetimePlayers().getByKey(packet.recipient());
            PlayerRank recipientRank = network.getCachedPlayerRanks().get(packet.recipient());
            Component recipient = recipientRank.getPrefix().append(Component.text(recipientName));

            if (response.code().equals("ALREADY_SENT")) {
                player.sendMessage(Msg.whoops("You have already sent a friend request to ").append(recipient)
                    .append(Msg.mm("<gray>!")));
                return;
            }

            player.sendMessage(Msg.serverError("Failed to send your friend request to ").append(recipient)
                .append(Msg.mm("<gray>! Error: " + response.message())));

            Logger.error(
                "Failed to send " + packet.sender() + "'s friend request to " + packet.recipient() + "!. Error: "
                    + response.message() + " | Code: " + response.code());

        });
    }

    public void sendAcceptFriendRequest(UUID requestId) {
        new FriendApiIdProtocolObject.Packet(requestId).request(Subjects.FRIEND_ACCEPT_BY_ID,
            (response, throwable) -> handleAccept(response, throwable, null, null));
    }

    public void sendAcceptFriendRequest(UUID sender, UUID recipient) {
        new FriendNotifyPacket.Packet(sender, recipient).request(Subjects.FRIEND_ACCEPT,
            (response, throwable) -> handleAccept(response, throwable, recipient, sender));
    }

    private void handleAccept(FriendApiProtocolObject.Response response, @Nullable Throwable throwable,
        @Nullable UUID recipient, @Nullable UUID sender) {
        if (throwable != null) {
            if (recipient != null) {
                Cytosis.getPlayer(recipient)
                    .ifPresent(player -> player.sendMessage(Msg.serverError("Failed to process your friend request!")));
            }
            Logger.error("Internal error upon processing a friend acceptance.", throwable);
        }
        if (response.success()) {
            return;
        }

        String senderName = network.getLifetimePlayers().getByKey(sender);
        PlayerRank recipientRank = network.getCachedPlayerRanks().get(sender);
        Component senderComp = recipientRank.getPrefix().append(Component.text(senderName));

        if (response.message().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.whoops("You don't have an active friend request from ").append(senderComp)
                    .append(Msg.mm("<gray>!"))));
        }

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.serverError("Failed to process accepting your friend request: " + response.message())));
        }
        Logger.info("Failed to accept friend request: " + response.code());
    }

    public void sendDeclineFriendRequest(UUID requestId) {
        new FriendApiIdProtocolObject.Packet(requestId).request(Subjects.FRIEND_DECLINE_BY_ID,
            (response, throwable) -> handleDecline(response, throwable, null, null));
    }

    public void sendDeclineFriendRequest(UUID sender, UUID recipient) {
        new FriendNotifyPacket.Packet(sender, recipient).request(Subjects.FRIEND_DECLINE,
            (response, throwable) -> handleDecline(response, throwable, recipient, sender));
    }

    private void handleDecline(FriendApiProtocolObject.Response response, @Nullable Throwable throwable,
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

        if (response.message().equalsIgnoreCase("NOT_FOUND")) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.whoops("You don't have an active friend request from ").append(senderComp)
                    .append(Msg.mm("<gray>!"))));
        }

        if (response.success()) {
            return;
        }

        if (recipient != null) {
            Cytosis.getPlayer(recipient).ifPresent(player -> player.sendMessage(
                Msg.serverError("Failed to process declining your friend request: " + response.message())));
        }
        Logger.info("Failed to accept friend request: " + response.code());
    }

    public void sendFriendRemove(UUID sender, UUID recipient) {
        new FriendNotifyPacket.Packet(sender, recipient).publish(Subjects.FRIEND_REMOVE);
    }
}
