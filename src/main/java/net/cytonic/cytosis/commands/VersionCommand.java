package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
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
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.version"));
        setDefaultExecutor((sender, _) -> {
            if (sender.hasPermission("cytosis.commands.version")) {
                sender.sendMessage(MM."<YELLOW>Running Cytosis v\{Cytosis.VERSION}!");
            }
        });
    }
}
