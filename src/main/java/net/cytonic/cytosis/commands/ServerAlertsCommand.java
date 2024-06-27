package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class ServerAlertsCommand extends Command {

    public ServerAlertsCommand() {
        super("serveralerts");
        setCondition(((sender, _) -> sender.hasPermission("cytosis.commands.serveralerts")));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof Player player) {
                if (player.hasPermission("cytosis.commands.serveralerts")) {
                    if (!Cytosis.getCytonicNetwork().getServerAlerts().get(player.getUuid())) {
                        player.sendMessage(MM."<GREEN>Server alerts are now enabled!");
                        Cytosis.getCytonicNetwork().getServerAlerts().replace(player.getUuid(),true);
                        Cytosis.getDatabaseManager().getMysqlDatabase().setServerAlerts(player.getUuid(),true);
                    } else {
                        player.sendMessage(MM."<RED>Server alerts are now disabled!");
                        Cytosis.getCytonicNetwork().getServerAlerts().replace(player.getUuid(), false);
                        Cytosis.getDatabaseManager().getMysqlDatabase().setServerAlerts(player.getUuid(), false);
                    }
                }
            }
        });
    }
}
