package net.cytonic.cytosis.commands.server;

import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;
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
        try {
            setCondition(CommandUtils.IS_STAFF);
            setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.mm("<RED>You must specify a server!")));
            ArgumentWord serverArgument = ArgumentType.Word("server");
            serverArgument.setCallback((sender, exception) -> sender.sendMessage(
                Component.text("The server " + exception.getInput() + " is invalid!", NamedTextColor.RED)));
            serverArgument.setSuggestionCallback((cmds, cmdc, suggestion) -> {
                List<SuggestionEntry> options = Cytosis.get(CytonicNetwork.class).getServers().values()
                    .stream()
                    .map(cytonicServer -> new SuggestionEntry(cytonicServer.id()))
                    .toList();
                CommandUtils.filterEntries(cmdc.get(serverArgument), options).forEach(suggestion::addEntry);
            });
            addSyntax(((sender, context) -> {
                if (sender instanceof CytosisPlayer player) {
                    if (!player.isStaff()) return;
                    if (!context.get(serverArgument).equalsIgnoreCase(CytosisContext.SERVER_ID)) {
                        for (CytonicServer server : Cytosis.get(CytonicNetwork.class).getServers()
                            .values()) {
                            if (server.id().equals(context.get(serverArgument))) {
                                player.sendMessage(
                                    Component.text("Connecting to " + server.id(), NamedTextColor.GREEN));
                                //todo: instance?
                                Cytosis.get(NatsManager.class)
                                    .sendPlayerToServer(player.getUuid(), server, null);
                            }
                        }
                    } else {
                        player.sendMessage(Msg.mm("<RED>You are already connected to the server!"));
                    }
                }
            }), serverArgument);
        } catch (Exception e) {
            Logger.error("error", e);
        }
    }
}