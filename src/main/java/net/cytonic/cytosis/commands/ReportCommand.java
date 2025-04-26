package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

/**
 * A command for reporting players
 */
public class ReportCommand extends CytosisCommand {

    /**
     * A command to report players
     */
    public ReportCommand() {
        super("report");

        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching online players..."));
            }
            Cytosis.getCytonicNetwork().getOnlinePlayers().getValues().forEach(player ->
                    suggestion.addEntry(new SuggestionEntry(player)));
        });
        setDefaultExecutor((sender, ignored) -> {
            if (sender instanceof final Player player) {
                player.sendMessage(Msg.mm("<RED>Usage: /report (player)"));
            } else {
                sender.sendMessage(Msg.mm("<RED>Only players may execute this command!"));
            }
        });
        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                String playerName = context.get(playerArg);
                /*for (PlayerServer server : Cytosis.getCytonicNetwork().getNetworkPlayersOnServers().values()) {
                    if (server.playerName().equalsIgnoreCase(playerName)) {

                        player.sendMessage(Msg.mm("<RED>Coming soon!"));
                        return;
                        // execute stuff here
                    }
                }*/

                player.sendMessage(Msg.mm("<RED>Player not found!"));
            }
        }, playerArg);
    }
}
