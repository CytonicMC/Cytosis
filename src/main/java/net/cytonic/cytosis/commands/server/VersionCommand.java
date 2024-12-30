package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.minestom.server.command.builder.Command;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * The class representing the version command
 */
public class VersionCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public VersionCommand() {
        super("version", "ver");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<yellow>Running Cytosis v\{Cytosis.VERSION}!"));
    }
}
