package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.minestom.server.command.builder.Command;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A command to toggle server alerts for when they start and stop
 */
public class ServerAlertsCommand extends Command {

    /**
     * A command to toggle server alerts
     */
    public ServerAlertsCommand() {
        super("serveralerts");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof CytosisPlayer player) {

                if (!Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.SERVER_ALERTS)) {
                    player.sendMessage(MM."<green>Server alerts are now enabled!");
                    Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.SERVER_ALERTS, true);
                } else {
                    player.sendMessage(MM."<red>Server alerts are now disabled!");
                    Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.SERVER_ALERTS, false);
                }

            }
        });
    }
}
