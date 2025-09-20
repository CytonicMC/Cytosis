package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;

/**
 * A command to toggle server alerts for when they start and stop
 */
public class ServerAlertsCommand extends CytosisCommand {

    /**
     * A command to toggle server alerts
     */
    public ServerAlertsCommand() {
        super("serveralerts");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, ignored) -> {
            if (sender instanceof CytosisPlayer player) {
                PreferenceManager preferenceManager = Cytosis.CONTEXT.getComponent(PreferenceManager.class);
                if (!preferenceManager.getPlayerPreference(player.getUuid(), CytosisPreferences.SERVER_ALERTS)) {
                    player.sendMessage(Msg.mm("<green>Server alerts are now enabled!"));
                    preferenceManager.updatePlayerPreference(player.getUuid(), CytosisNamespaces.SERVER_ALERTS, true);
                } else {
                    player.sendMessage(Msg.mm("<red>Server alerts are now disabled!"));
                    preferenceManager.updatePlayerPreference(player.getUuid(), CytosisNamespaces.SERVER_ALERTS, false);
                }
            }
        });
    }
}