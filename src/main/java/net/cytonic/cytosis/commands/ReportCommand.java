package net.cytonic.cytosis.commands;

import net.minestom.server.entity.Player;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

/**
 * A command for reporting players
 */
public class ReportCommand extends CytosisCommand {

    /**
     * A command to report players
     */
    public ReportCommand() {
        super("report");

        setDefaultExecutor((sender, ignored) -> {
            if (sender instanceof final Player player) {
                player.sendMessage(Msg.mm("<RED>Usage: /report (player)"));
            } else {
                sender.sendMessage(Msg.mm("<RED>Only players may execute this command!"));
            }
        });
        addSyntax((sender, context) -> {
            if (sender instanceof final Player player) {
                String playerName = context.get(CommandUtils.NETWORK_PLAYERS);
                /*for (PlayerServer server : Cytosis.getCytonicNetwork().getNetworkPlayersOnServers().values()) {
                    if (server.playerName().equalsIgnoreCase(playerName)) {

                        player.sendMessage(Msg.mm("<RED>Coming soon!"));
                        return;
                        // execute stuff here
                    }
                }*/

                player.sendMessage(Msg.mm("<RED>Player not found!"));
            }
        }, CommandUtils.NETWORK_PLAYERS);
    }
}
