package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

class NickResetCommand extends CytosisCommand {

    NickResetCommand() {
        super("reset");
        setCondition(CommandUtils.withRankOrStaff(PlayerRank.NEXUS));
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (!player.isNicked()) {
                player.whoops("You are not currently nicked!");
                return;
            }
            Cytosis.get(NicknameManager.class).disableNickname(player.getUuid());
            player.sendMessage(Msg.splash("UNNICKED!", "#BE9025",
                "Your apparent name, rank, and skin have been reset to your normal self."));
        });
    }
}