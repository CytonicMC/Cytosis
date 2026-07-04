package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.nicknames.NicknameManager.NicknameData;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.OfflinePlayer;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.cytosis.utils.Players;
import net.cytonic.cytosis.utils.Preferences;

public class FriendAddCommand extends CytosisCommand {

    public FriendAddCommand() {
        super("add");

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            UUID target = PlayerUtils.resolveUuid(context.get(FriendCommand.NON_FRIEND_ARG));
            OfflinePlayer targetObj;
            try {
                targetObj = Players.offline(target);
            } catch (NullPointerException e) {
                player.whoops("The player '%s' doesn't exist!", context.get(FriendCommand.NON_FRIEND_ARG));
                return;
            }

            if (targetObj.uuid().equals(player.getUuid())) {
                player.whoops("You cannot add yourself as a friend!");
                return;
            }

            CytonicNetwork network = Cytosis.get(CytonicNetwork.class);
            NicknameData nickData = targetObj.getPreference(Preferences.NICKNAME_DATA);

            if (nickData != null) {
                player.whoops("%s is not accepting friend requests!", Players.miniName(targetObj.uuid()));
                return;
            }

            String name = Players.trueMiniName(targetObj.uuid());

            if (!network.getOnlinePlayers().containsKey(target)) {
                player.whoops("%s is not online!", name);
                return;
            }

            if (!targetObj.getPreference(Preferences.ACCEPT_FRIEND_REQUESTS)) {
                player.whoops("%s does not accept friend requests!", name);
                return;
            }

            if (player.getFriends().contains(target)) {
                player.whoops("You are already friends with %s!", name);
                return;
            }

            player.sendFriendRequest(target);
        }, FriendCommand.NON_FRIEND_ARG);
    }

}
