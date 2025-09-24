package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;

public class FriendDeclineCommand extends CytosisCommand {

    public FriendDeclineCommand() {
        super("decline");

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            UUID target = PlayerUtils.resolveUuid(context.get(FriendCommand.NON_FRIEND_ARG));
            if (target == null) {
                player.sendMessage(Msg.whoops("The player '%s' doesn't exist!",
                    context.get(FriendCommand.NON_FRIEND_ARG)));
                return;
            }

            if (target.equals(player.getUuid())) {
                player.sendMessage(Msg.whoops("You cannot accept your own friend request!"));
                return;
            }
            CytonicNetwork network = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
            String name = network.getLifetimePlayers().getByKey(target);
            Component targetComp = network.getCachedPlayerRanks().get(target).getPrefix().append(Component.text(name));

            if (Cytosis.CONTEXT.getComponent(FriendManager.class).getFriends(player.getUuid()).contains(target)) {
                player.sendMessage(
                    Msg.whoops("You are already friends with ").append(targetComp).append(Msg.grey("!")));
                return;
            }

            player.declineFriendRequest(target);
        }, FriendCommand.NON_FRIEND_ARG);
    }

}
