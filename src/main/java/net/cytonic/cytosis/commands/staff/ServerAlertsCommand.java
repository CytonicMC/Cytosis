package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

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
            if (!(sender instanceof CytosisPlayer player)) return;
            player.togglePreference(Preferences.SERVER_ALERTS, () ->
                    player.sendMessage(Msg.red("Server alerts are now disabled!")),
                () -> player.sendMessage(Msg.green("Server alerts are now enabled!"))
            );
        });
    }
}