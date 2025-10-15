package net.cytonic.cytosis.commands.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.utils.Msg;

/**
 * The class representing the broadcast command
 */
public class BroadcastCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public BroadcastCommand() {
        super("broadcast", "bc");
        setCondition(CommandUtils.IS_ADMIN);
        var broadcastArgument = ArgumentType.StringArray("broadcastArgument");
        var serverArgument = ArgumentType.Word("type").from("all", "this");
        serverArgument.setSuggestionCallback((cmds, cmdc, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("all"));
            suggestion.addEntry(new SuggestionEntry("this"));
        });
        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.whoops("Usage: /broadcast (message)")));
        addSyntax((sender, context) -> {
            if (!Cytosis.getOnlinePlayers().isEmpty()) {
                Component broadcast = Msg.aquaSplash("Broadcast",
                    "» <white>" + String.join(" ", context.get(broadcastArgument)));
                if (context.get(serverArgument).equalsIgnoreCase("this")) {
                    Cytosis.getOnlinePlayers().forEach(player -> player.sendMessage(broadcast));
                } else if (context.get(serverArgument).equalsIgnoreCase("all")) {
                    Cytosis.CONTEXT.getComponent(RedisDatabase.class).sendBroadcast(broadcast);
                }
            }
        }, serverArgument, broadcastArgument);
    }
}