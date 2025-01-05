package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * A command for reporting players
 */
public class ReportCommand extends Command {

    /**
     * A command to report players
     */
    public ReportCommand() {
        super("report");

        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, _, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(MM."<green>Fetching online players...");
            }
            Cytosis.getCytonicNetwork().getOnlinePlayers().getValues().forEach(player ->
                    suggestion.addEntry(new SuggestionEntry(player)));
        });
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final Player player) {
                player.sendMessage(MM."<RED>Usage: /report (player)");
            } else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                String playerName = context.get(playerArg);
                /*for (PlayerServer server : Cytosis.getCytonicNetwork().getNetworkPlayersOnServers().values()) {
                    if (server.playerName().equalsIgnoreCase(playerName)) {

                        player.sendMessage(MM."<RED>Coming soon!");
                        return;
                        // execute stuff here
                    }
                }*/

                player.sendMessage(MM."<RED>Player not found!");
            }
        }, playerArg);
    }
}
