package net.cytonic.cytosis.commands;

import net.minestom.server.command.builder.Command;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * The class representing the hello command
 */
public class HelloCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public HelloCommand() {
        super("hello", "hi");
        setDefaultExecutor((sender, _) -> {
            sender.sendMessage(MM."<GREEN>Hello!");
        });
    }
}