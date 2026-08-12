package net.cytonic.cytosis.commands.debug.cooldowns;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;

@SubCommand(CooldownCommand.class)
public class ClearCooldownCommand extends CytosisCommand {

    ClearCooldownCommand() {
        super("clear");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);
    }
}
