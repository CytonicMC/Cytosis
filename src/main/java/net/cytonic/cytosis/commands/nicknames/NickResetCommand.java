package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class NickResetCommand extends CytosisCommand {
    public NickResetCommand() {
        super("reset");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (!player.isNicked()) {
                player.sendMessage(Msg.whoops("You are not currently nicked!"));
                return;
            }
            Cytosis.CONTEXT.getComponent(NicknameManager.class).disableNickname(player.getUuid());
            player.sendMessage(Msg.splash("UNNICKED!", "#BE9025", "Your apparent name, rank, and skin have been reset to your nomal self."));
        });
    }
}