package net.cytonic.cytosis.commands.server;

import java.util.List;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

/**
 * A class representing the server command
 */
public class ServerCommand extends CytosisCommand {

    /**
     * Sends players to a server from the command
     */
    public ServerCommand() {
        super("cytosis:server", "server");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, _) -> sender.sendMessage(Msg.whoops("You must specify a server!")));
        ArgumentWord serverArgument = ArgumentType.Word("server");
        serverArgument.setSuggestionCallback((_, ctx, suggestion) -> {
            List<SuggestionEntry> options = Cytosis.get(CytonicNetwork.class).getServers().keySet()
                .stream()
                .filter(s -> !s.equalsIgnoreCase(CytosisContext.SERVER_ID))
                .map(SuggestionEntry::new)
                .toList();
            CommandUtils.filterEntries(ctx.get(serverArgument), options).forEach(suggestion::addEntry);
        });
        addSyntax(((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            if (!player.isStaff()) return;
            if (context.get(serverArgument).equalsIgnoreCase(CytosisContext.SERVER_ID)) {
                player.sendMessage(Msg.whoops("You are already connected to %s!", CytosisContext.SERVER_ID));
                return;
            }
            player.sendToServer(context.get(serverArgument));
        }), serverArgument);
    }
}