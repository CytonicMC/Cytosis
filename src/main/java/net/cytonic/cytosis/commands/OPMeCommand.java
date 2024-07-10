package net.cytonic.cytosis.commands;

import net.minestom.server.command.builder.Command;
import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the opme command
 */
public class OPMeCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public OPMeCommand() {
        super("opme", "op");
        setDefaultExecutor((sender, _) -> {
            sender.sendMessage(MM."<YELLOW>You're now an operator!");
        });
    }
}
