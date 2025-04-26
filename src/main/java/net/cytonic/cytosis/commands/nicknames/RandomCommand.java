package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.nicknames.NicknameGenerator;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class RandomCommand extends CytosisCommand {
    public RandomCommand() {
        super("random");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.getNicknameManager().nicknamePlayer(player.getUuid(), NicknameGenerator.generateNicknameData());
            player.sendMessage(Msg.goldSplash("DISGUISED!", "Your apparent name, rank, and skin have been randomized. To go back to your normal self, use the <#BE9025>/nick reset</#BE9025> command."));
        });
    }
}
