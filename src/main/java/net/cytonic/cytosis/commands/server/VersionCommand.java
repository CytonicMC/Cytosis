package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;

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
        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.mm("<yellow>Running Cytosis v" + Cytosis.VERSION + "!")));
    }
}
