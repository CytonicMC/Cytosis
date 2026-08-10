package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.OfflinePlayer;
import net.cytonic.cytosis.utils.Players;

@SubCommand
class DeclineCommand extends CytosisCommand {

    DeclineCommand() {
        super("decline");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String raw = context.get(FriendCommand.NON_FRIEND_ARG);
            UUID uuid = Players.resolveNickedUuid(raw);
            if (uuid != null) { // this player is nicked
                player.whoops("You don't have an active friend request from %s!", Players.miniNameFragile(raw));
                return;
            }

            UUID target = Players.resolveUuid(context.get(FriendCommand.NON_FRIEND_ARG));
            OfflinePlayer targetObj;
            try {
                targetObj = Players.offline(target);
            } catch (NullPointerException e) {
                player.whoops("The player '%s' doesn't exist!", context.get(FriendCommand.NON_FRIEND_ARG));
                return;
            }

            if (targetObj.uuid().equals(player.getUuid())) {
                player.whoops("You cannot accept your own friend request!");
                return;
            }

            String targetName = Players.miniNameFragile(context.get(FriendCommand.NON_FRIEND_ARG));

            if (player.getFriends().contains(target)) {
                player.whoops("You are already friends with %s!", targetName);
                return;
            }

            player.declineFriendRequest(target);
        }, FriendCommand.NON_FRIEND_ARG);
    }
}
