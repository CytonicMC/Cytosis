package net.cytonic.cytosis.commands.debug.cooldowns;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;

@SubCommand(CooldownCommand.class)
public class GetCooldownCommand extends CytosisCommand {

    GetCooldownCommand() {
        super("get");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);
    }
}
