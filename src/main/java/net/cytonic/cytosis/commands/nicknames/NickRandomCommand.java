package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.nicknames.NicknameGenerator;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class NickRandomCommand extends CytosisCommand {

    public NickRandomCommand() {
        super("random");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;

            if (player.isNicked()) {
                player.sendMessage(Msg.whoops("You are already nicked!"));
                return;
            }

            Cytosis.CONTEXT.getComponent(NicknameManager.class)
                .nicknamePlayer(player.getUuid(), NicknameGenerator.generateNicknameData());
            player.sendMessage(Msg.goldSplash("DISGUISED!", """
                Your apparent name, rank, and skin have been randomized. To go back to your
                normal self, use the <#BE9025>/nick reset</#BE9025> command.
                """));
        });
    }
}