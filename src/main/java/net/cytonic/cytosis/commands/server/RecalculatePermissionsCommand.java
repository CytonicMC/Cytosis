package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;

public class RecalculatePermissionsCommand extends CytosisCommand {

    public RecalculatePermissionsCommand() {
        super("recalculatepermissions", "recalcperms");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.get(CommandHandler.class).recalculateCommands(player);
        });
    }
}