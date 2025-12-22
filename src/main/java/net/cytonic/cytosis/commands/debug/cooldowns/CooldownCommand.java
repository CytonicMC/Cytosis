package net.cytonic.cytosis.commands.debug.cooldowns;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.CommandExecutor;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;

public class CooldownCommand extends CytosisCommand {

    public static final CommandExecutor HELP_EXECUTOR = (sender, context) -> Cytosis.get(
        CommandManager.class).execute(sender, "cooldown help");

    public CooldownCommand() {
        super("cooldown");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor(HELP_EXECUTOR);

        addSubcommand(new CooldownHelpCommand());
        addSubcommand(new SetCooldownCommand());
        addSubcommand(new GetCooldownCommand());
        addSubcommand(new ClearCooldownCommand());
    }
}
