package net.cytonic.cytosis.commands.friends;

import java.util.UUID;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.nicknames.NicknameManager.NicknameData;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;

public class FriendAddCommand extends CytosisCommand {

    public FriendAddCommand() {
        super("add");

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            UUID target = PlayerUtils.resolveUuid(context.get(FriendCommand.NON_FRIEND_ARG));
            if (target == null) {
                player.sendMessage(Msg.whoops("The player '%s' doesn't exist!",
                    context.get(FriendCommand.NON_FRIEND_ARG)));
                return;
            }

            if (target.equals(player.getUuid())) {
                player.sendMessage(Msg.whoops("You cannot add yourself as a friend!"));
                return;
            }

            boolean nicked = Cytosis.getNicknameManager().isNicked(target);
            String name = Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(target);
            Component targetComp = Cytosis.getCytonicNetwork().getCachedPlayerRanks().get(target).getPrefix()
                .append(Component.text(name));

            if (nicked) {
                NicknameData data = Cytosis.getNicknameManager().getData(target);
                targetComp = data.rank().getPrefix().append(Component.text(data.nickname()));
            }

            if (!Cytosis.getCytonicNetwork().getOnlinePlayers().containsKey(target) && !nicked) {
                player.sendMessage(Msg.whoops("The player ").append(targetComp).append(Msg.grey(" is not online!")));
                return;
            }

            if (!Cytosis.getPreferenceManager()
                .getPlayerPreference(target, CytosisPreferences.ACCEPT_FRIEND_REQUESTS) || nicked) {
                player.sendMessage(Msg.whoops("").append(targetComp)
                    .append(Msg.mm("<gray> is not accepting friend requests!")));
                return;
            }

            if (Cytosis.getFriendManager().getFriends(player.getUuid()).contains(target)) {
                player.sendMessage(Msg.whoops("You are already friends with ").append(targetComp)
                    .append(Msg.mm("<gray>!")));
                return;
            }

            player.sendFriendRequest(target);
        }, FriendCommand.NON_FRIEND_ARG);
    }

}
