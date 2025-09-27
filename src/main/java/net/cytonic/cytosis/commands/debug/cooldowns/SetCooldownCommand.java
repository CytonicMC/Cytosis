package net.cytonic.cytosis.commands.debug.cooldowns;

import net.cytonic.cytosis.commands.debug.cooldowns.global.SetGlobalCommand;
import net.cytonic.cytosis.commands.debug.cooldowns.personal.SetPersonalCommand;
import net.cytonic.cytosis.commands.utils.CytosisCommand;

public class SetCooldownCommand extends CytosisCommand {

    public SetCooldownCommand() {
        super("set");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);
        addSubcommand(new SetGlobalCommand());
        addSubcommand(new SetPersonalCommand());
    }
}
