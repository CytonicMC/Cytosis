package net.cytonic.cytosis.commands.debug.cooldowns;

import net.cytonic.cytosis.commands.debug.cooldowns.global.GetGlobalCommand;
import net.cytonic.cytosis.commands.debug.cooldowns.personal.GetPersonalCommand;
import net.cytonic.cytosis.commands.utils.CytosisCommand;

public class GetCooldownCommand extends CytosisCommand {

    public GetCooldownCommand() {
        super("get");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);
        addSubcommand(new GetGlobalCommand());
        addSubcommand(new GetPersonalCommand());
    }
}
