package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.nicknames.NicknameManager.NicknameData;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.OfflinePlayer;
import net.cytonic.cytosis.utils.Msg;
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
                player.sendMessage(
                    Msg.whoops("The player '%s' doesn't exist!", context.get(FriendCommand.NON_FRIEND_ARG)));
                return;
            }

            if (targetObj.uuid().equals(player.getUuid())) {
                player.sendMessage(Msg.whoops("You cannot add yourself as a friend!"));
                return;
            }

            CytonicNetwork network = Cytosis.get(CytonicNetwork.class);
            NicknameData nickData = targetObj.getPreference(Preferences.NICKNAME_DATA);

            if (nickData != null) {
                player.sendMessage(
                    Msg.whoops("%s is not accepting friend requests!", Players.miniName(targetObj.uuid())));
                return;
            }

            String name = Players.trueMiniName(targetObj.uuid());

            if (!network.getOnlinePlayers().containsKey(target)) {
                player.sendMessage(Msg.whoops("%s is not online!", name));
                return;
            }

            if (!Cytosis.get(PreferenceManager.class).getPlayerPreference(target, Preferences.ACCEPT_FRIEND_REQUESTS)) {
                player.sendMessage(Msg.whoops("%s does not accept friend requests!", name));
                return;
            }

            if (Cytosis.get(FriendManager.class).getFriends(player.getUuid()).contains(target)) {
                player.sendMessage(Msg.whoops("You are already friends with %s!", name));
                return;
            }

            player.sendFriendRequest(target);
        }, FriendCommand.NON_FRIEND_ARG);
    }

}
