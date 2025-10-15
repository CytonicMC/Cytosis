package net.cytonic.cytosis.commands.debug.cooldowns;

import net.cytonic.cytosis.commands.debug.cooldowns.global.ClearGlobalCommand;
import net.cytonic.cytosis.commands.debug.cooldowns.personal.ClearPersonalCommand;
import net.cytonic.cytosis.commands.utils.CytosisCommand;

public class ClearCooldownCommand extends CytosisCommand {

    public ClearCooldownCommand() {
        super("clear");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);
        addSubcommand(new ClearGlobalCommand());
        addSubcommand(new ClearPersonalCommand());
    }
}
