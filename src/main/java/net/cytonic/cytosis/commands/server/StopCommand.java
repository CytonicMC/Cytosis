package net.cytonic.cytosis.commands.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.ServerInstancingManager;
import net.cytonic.cytosis.utils.Msg;

/**
 * A class representing the stop command
 */
public class StopCommand extends CytosisCommand {

    /**
     * A simple command to stop the server
     */
    public StopCommand() {
        super("stop");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(
            Msg.mm("<red>Are you sure you want to stop the server? If so add confirm to the command")));
        var confirmArgument = ArgumentType.Word("confirmArgument").from("confirm");
        confirmArgument.setSuggestionCallback(
            ((cmds, cmdc, suggestion) -> suggestion.addEntry(new SuggestionEntry("confirm"))));
        addSyntax((sender, context) -> {
            if (context.get(confirmArgument).equalsIgnoreCase("confirm")) {
                if (Cytosis.IS_NOMAD) {
                    Cytosis.get(ServerInstancingManager.class).deleteThisServerInstance();
                    sender.sendMessage(Msg.success("Dispatched the shutdown of this instance!"));
                    return;
                }
                sender.sendMessage("Stopping the server...");
                MinecraftServer.stopCleanly();
            }
        }, confirmArgument);
    }
}