package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.nicknames.NicknameGenerator;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

class NickRandomCommand extends CytosisCommand {

    NickRandomCommand() {
        super("random");
        setCondition(CommandUtils.withRankOrStaff(PlayerRank.NEXUS));
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            if (player.isNicked()) {
                player.whoops("You are already nicked!");
                return;
            }

            Cytosis.get(NicknameManager.class)
                .nicknamePlayer(player.getUuid(), NicknameGenerator.generateNicknameData());
            player.sendMessage(Msg.goldSplash("DISGUISED!", """
                Your apparent name, rank, and skin have been randomized. To go back to your \
                normal self, use the <#BE9025>/nick reset</#BE9025> command.
                """));
        });
    }
}