package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.OfflinePlayer;
import net.cytonic.cytosis.utils.Players;

@SubCommand
class AcceptCommand extends CytosisCommand {

    AcceptCommand() {
        super("accept");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String raw = context.get(FriendCommand.NON_FRIEND_ARG);
            UUID uuid = Players.resolveNickedUuid(raw);
            if (uuid != null) { // this player is nicked
                player.whoops("You don't have an active friend request from %s!", Players.miniNameFragile(raw));
                return;
            }

            UUID target = Players.resolveUuid(raw);
            OfflinePlayer targetObj;
            try {
                targetObj = Players.offline(target);
            } catch (NullPointerException e) {
                player.whoops("The player '%s' doesn't exist!", raw);
                return;
            }

            if (targetObj.uuid().equals(player.getUuid())) {
                player.whoops("You cannot accept your own friend request!");
                return;
            }

            String targetName = Players.miniNameFragile(raw);

            if (player.getFriends().contains(target)) {
                player.whoops("You are already friends with %s", targetName);
                return;
            }

            player.acceptFriendRequest(target);
        }, FriendCommand.NON_FRIEND_ARG);
    }
}
