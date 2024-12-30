package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.commands.CommandUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A class representing the stop command
 */
public class StopCommand extends Command {

    /**
     * A simple command to stop the server
     */
    public StopCommand() {
        super("stop");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Are you sure you want to stop the server? If so add confirm to the command"));
        var confirmArgument = ArgumentType.Word("confirmArgument").from("confirm");
        confirmArgument.setSuggestionCallback(((_, _, suggestion) -> suggestion.addEntry(new SuggestionEntry("confirm"))));
        addSyntax((sender, context) -> {
            if (context.get(confirmArgument).equalsIgnoreCase("confirm")) {
                sender.sendMessage("Stopping the server...");
                MinecraftServer.stopCleanly();
            }
        }, confirmArgument);
    }
}