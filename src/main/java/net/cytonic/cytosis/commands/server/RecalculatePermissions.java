package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;

public class RecalculatePermissions extends CytosisCommand {

    public RecalculatePermissions() {
        super("recalculatepermissions", "recalcperms");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.getCommandHandler().recalculateCommands(player);
        });
    }
}
