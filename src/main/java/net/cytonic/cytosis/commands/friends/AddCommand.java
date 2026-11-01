package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.OfflinePlayer;
import net.cytonic.cytosis.utils.Players;
import net.cytonic.cytosis.utils.Preferences;

@SubCommand(FriendCommand.class)
class AddCommand extends CytosisCommand {

    AddCommand() {
        super("add");
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String raw = context.get(FriendCommand.NON_FRIEND_ARG);
            UUID uuid = Players.resolveNickedUuid(raw);
            if (uuid != null) { // this player is nicked
                player.whoops("%s does not accept friend requests!", Players.miniNameFragile(raw));
                return;
            }

            uuid = Players.resolveUuid(context.get(FriendCommand.NON_FRIEND_ARG));
            OfflinePlayer targetObj;
            try {
                targetObj = Players.offline(uuid);
            } catch (NullPointerException e) {
                player.whoops("The player '%s' doesn't exist!", context.get(FriendCommand.NON_FRIEND_ARG));
                return;
            }

            if (targetObj.uuid().equals(player.getUuid())) {
                player.whoops("You cannot add yourself as a friend!");
                return;
            }

            CytonicNetwork network = Cytosis.get(CytonicNetwork.class);

            String name = Players.trueMiniName(targetObj.uuid());

            if (player.getFriends().contains(uuid)) {
                player.whoops("You are already friends with %s!", name);
                return;
            }

            if (!network.getOnlinePlayers().containsKey(uuid)) {
                player.whoops("%s is not online!", name);
                return;
            }

            if (!targetObj.getPreference(Preferences.ACCEPT_FRIEND_REQUESTS)) {
                player.whoops("%s does not accept friend requests!", name);
                return;
            }

            player.sendFriendRequest(uuid);
        }, FriendCommand.NON_FRIEND_ARG);
    }
}
