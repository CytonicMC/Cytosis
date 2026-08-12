package net.cytonic.cytosis.commands.nicknames;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.player.CytosisPlayer;

public class NickCommand extends CytosisCommand {

    public NickCommand() {
        super("nick", "nickname");
        setCondition(CommandUtils.withRankOrStaff(PlayerRank.NEXUS));

        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.dispatchCommand("nick help");
        });
    }
}
