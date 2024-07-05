package net.cytonic.cytosis.commands;

import net.minestom.server.command.builder.Command;
import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class HelloCommand extends Command {
    public HelloCommand() {
        super("hello");
        addSyntax((sender, _) -> {
            sender.sendMessage(MM."<GREEN>Hello!");
        });
    }
}