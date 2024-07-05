package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.utils.MiniMessageTemplate;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

/**
 * A class representing the stop command
 */
public class StopCommand extends Command {

    /**
     * A simple command to stop the server
     */
    public StopCommand() {
        super("stop");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.stop"));
        setDefaultExecutor((sender, _) -> sender.sendMessage(MiniMessageTemplate.MM."<RED>Are you sure you want to stop the server? If so add confirm to the command"));
        var confirmArgument = ArgumentType.Word("confirmArgument").from("confirm");
        confirmArgument.setSuggestionCallback(((_, _, suggestion) -> suggestion.addEntry(new SuggestionEntry("confirm"))));
        addSyntax((sender, context) -> {
            if (sender.hasPermission("cytonic.commands.stop")) {
                if (context.get(confirmArgument).equalsIgnoreCase("confirm")) {
                    sender.sendMessage("Stopping the server...");
                    MinecraftServer.stopCleanly();
                }
            }
        }, confirmArgument);
    }
}