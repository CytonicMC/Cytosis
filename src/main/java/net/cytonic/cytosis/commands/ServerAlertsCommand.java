package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.CytosisPreferences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.NamespaceID;
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
        setCondition(((sender, _) -> sender.hasPermission("cytosis.commands.serveralerts")));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof Player player) {
                if (player.hasPermission("cytosis.commands.serveralerts")) {
                    if (!Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.SERVER_ALERTS)) {
                        player.sendMessage(MM."<GREEN>Server alerts are now enabled!");
                        Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), NamespaceID.from("cytosis:server_alerts"), true);
                    } else {
                        player.sendMessage(MM."<RED>Server alerts are now disabled!");
                        Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), NamespaceID.from("cytosis:server_alerts"), false);
                    }
                }
            }
        });
    }
}
