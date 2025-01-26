package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

/**
 * A class representing the server command
 */
public class ServerCommand extends Command {

    /**
     * Sends players to a server from the command
     */
    public ServerCommand() {
        super("cytosis:server", "server");
        try {
            setCondition(CommandUtils.IS_STAFF);
            setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.mm("<RED>You must specify a server!")));
            var serverArgument = ArgumentType.Word("server");
            serverArgument.setCallback((sender, exception) -> sender.sendMessage(Component.text("The server " + exception.getInput() + " is invalid!", NamedTextColor.RED)));
            serverArgument.setSuggestionCallback((cmds, cmdc, suggestion) -> {
                for (CytonicServer server : Cytosis.getCytonicNetwork().getServers().values()) {
                    suggestion.addEntry(new SuggestionEntry(server.id()));
                }
            });
            addSyntax(((sender, context) -> {
                if (sender instanceof CytosisPlayer player) {
                    if (!player.isStaff()) return;
                    if (!context.get(serverArgument).equalsIgnoreCase(Cytosis.SERVER_ID)) {
                        for (CytonicServer server : Cytosis.getCytonicNetwork().getServers().values()) {
                            if (server.id().equals(context.get(serverArgument))) {
                                player.sendMessage(Component.text("Connecting to " + server.id(), NamedTextColor.GREEN));
                                //todo: instance?
                                Cytosis.getNatsManager().sendPlayerToServer(player.getUuid(), server, null);
                            }
                        }
                    } else player.sendMessage(Msg.mm("<RED>You are already connected to the server!"));
                }
            }), serverArgument);
        } catch (Exception e) {
            Logger.error("error", e);
        }
    }

}
