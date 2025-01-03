package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * The class representing the broadcast command
 */
public class BroadcastCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public BroadcastCommand() {
        super("broadcast", "bc");
        setCondition(CommandUtils.IS_ADMIN);
        var broadcastArgument = ArgumentType.StringArray("broadcastArgument");
        var serverArgument = ArgumentType.Word("type").from("all", "this");
        serverArgument.setSuggestionCallback((_, _, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("all"));
            suggestion.addEntry(new SuggestionEntry("this"));
        });
        setDefaultExecutor((sender, _) -> sender.sendMessage(MM."<RED>Usage: /broadcast (message)"));
        addSyntax((sender, context) -> {
                if (!Cytosis.getOnlinePlayers().isEmpty()) {
                    Component broadcast = MM."<aqua><b>Broadcast</b></aqua> <gray>»</gray> <white>\{String.join(" ", context.get(broadcastArgument))}";
                    if (context.get(serverArgument).equalsIgnoreCase("this")) {
                        Cytosis.getOnlinePlayers().forEach(player -> player.sendMessage(broadcast));
                    } else if (context.get(serverArgument).equalsIgnoreCase("all")) {
                        Cytosis.getDatabaseManager().getRedisDatabase().sendBroadcast(broadcast);
                    }
                }
        }, serverArgument, broadcastArgument);
    }
}
