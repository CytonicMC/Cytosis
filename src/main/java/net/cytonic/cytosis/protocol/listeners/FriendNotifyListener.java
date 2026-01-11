package net.cytonic.cytosis.protocol.listeners;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.messaging.Subjects;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.NotifyHandler;
import net.cytonic.protocol.notifyPackets.FriendNotifyPacket;

public class FriendNotifyListener {

    public static final Component FRIEND_LINE = Msg.darkAqua(
        "<st>                                                                                 ");
    private final FriendManager friendManager = Cytosis.get(FriendManager.class);
    private final CytonicNetwork network = Cytosis.get(CytonicNetwork.class);

    @NotifyHandler(subject = Subjects.FRIEND_ACCEPTANCE_NOTIFY)
    private void handleFriendAccept(FriendNotifyPacket.Packet packet) {
        Component target = createTargetComponent(packet.recipient());
        Component sender = createSenderComponent(packet.sender());

        for (Player player : Cytosis.getOnlinePlayers()) {
            if (player.getUuid().equals(packet.recipient())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(
                    Msg.mm("<aqua>You accepted ").append(sender).append(Msg.mm("<aqua>'s friend request!")));
                player.sendMessage(FRIEND_LINE);

                friendManager.addFriend(packet.sender(), packet.recipient());
            } else if (player.getUuid().equals(packet.sender())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(target.append(Msg.mm("<aqua> accepted your friend request!")));
                player.sendMessage(FRIEND_LINE);
                friendManager.addCachedFriend(packet.recipient(), packet.sender());
            }
        }
    }

    @NotifyHandler(subject = Subjects.FRIEND_DECLINATION_NOTIFY)
    private void handleFriendDecline(FriendNotifyPacket.Packet packet) {

        Component target = createTargetComponent(packet.recipient());
        Component sender = createSenderComponent(packet.sender());

        for (Player player : Cytosis.getOnlinePlayers()) {
            if (player.getUuid().equals(packet.recipient())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(
                    Msg.mm("<red>You declined ").append(sender).append(Msg.mm("<red>'s friend request!")));
                player.sendMessage(FRIEND_LINE);
            } else if (player.getUuid().equals(packet.sender())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(target.append(Msg.mm("<red> declined your friend request!")));
                player.sendMessage(FRIEND_LINE);
            }
        }
    }

    @NotifyHandler(subject = Subjects.FRIEND_EXPIRE_NOTIFY)
    private void handleFriendExpire(FriendNotifyPacket.Packet packet) {

        Component target = createTargetComponent(packet.recipient());
        Component sender = createSenderComponent(packet.sender());

        for (Player player : Cytosis.getOnlinePlayers()) {
            if (player.getUuid().equals(packet.recipient())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(
                    Msg.aqua("Your friend request from ").append(sender).append(Msg.aqua(" has expired!")));
                player.sendMessage(FRIEND_LINE);
            } else if (player.getUuid().equals(packet.sender())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(
                    Msg.aqua("Your friend request to ").append(target).append(Msg.aqua(" has expired!")));
                player.sendMessage(FRIEND_LINE);
            }
        }
    }

    @NotifyHandler(subject = Subjects.FRIEND_REQUEST_NOTIFY)
    private void handleFriendRequest(FriendNotifyPacket.Packet packet) {
        Component target = createTargetComponent(packet.recipient());
        Component sender = createSenderComponent(packet.sender());

        for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
            if (packet.recipient().equals(player.getUuid())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(sender.append(
                    Msg.mm("<aqua> sent you a friend request! You have 5 minutes to accept it! ").append(Msg.mm(
                        "<green><b><click:run_command:/friend accept " + packet.sender()
                            + ">[Accept]</click></b></green> <red><b><click:run_command:/friend decline "
                            + packet.sender() + ">[Decline]</click></b></red>"))));
                player.sendMessage(FRIEND_LINE);
            } else if (packet.sender().equals(player.getUuid())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(Msg.mm("<aqua>You send a friend request to ").append(target)
                    .append(Msg.mm("<aqua>! They have 5 minutes to accept it!")));
                player.sendMessage(FRIEND_LINE);
            }
        }
    }

    @NotifyHandler(subject = Subjects.FRIEND_REMOVE)
    private void handleFriendRemove(FriendNotifyPacket.Packet packet) {
        Component target = createTargetComponent(packet.recipient());
        Component sender = createSenderComponent(packet.sender());

        for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
            if (player.getUuid().equals(packet.recipient())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(sender.append(Msg.mm("<aqua> removed you from their friend list!")));
                player.sendMessage(FRIEND_LINE);
            } else if (player.getUuid().equals(packet.sender())) {
                player.sendMessage(FRIEND_LINE);
                player.sendMessage(
                    Msg.mm("<aqua>You removed ").append(target).append(Msg.mm(" from your friend list!")));
                player.sendMessage(FRIEND_LINE);
            }
        }
    }

    private Component createTargetComponent(UUID target) {
        String targetName = network.getLifetimePlayers().getByKey(target);
        PlayerRank targetRank = network.getCachedPlayerRanks().get(target);

        return targetRank.getPrefix().append(Component.text(targetName));
    }

    private Component createSenderComponent(UUID sender) {
        String senderName = network.getLifetimePlayers().getByKey(sender);
        PlayerRank senderRank = network.getCachedPlayerRanks().get(sender);

        return senderRank.getPrefix().append(Component.text(senderName));
    }
}
