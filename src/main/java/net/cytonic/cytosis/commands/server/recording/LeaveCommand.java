package net.cytonic.cytosis.commands.server.recording;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.replay.instance.ReplayInstance;

class LeaveCommand extends CytosisCommand {

    LeaveCommand() {
        super("exit");
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (!(player.getInstance() instanceof ReplayInstance i)) {
                player.whoops("You're not currently in a replay!");
                return;
            }
            i.leave(player);
        });
    }
}
