package net.cytonic.cytosis.commands.server;

import net.minestom.server.ServerFlag;
import net.minestom.server.command.builder.Command;
import net.minestom.server.monitoring.TickMonitor;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * The class representing the tps command
 */
public class TPSCommand extends Command {

    /**
     * A command for getting the current server tps
     */
    public TPSCommand(TickMonitor tickMonitor) {
        super("tps");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.tps"));
        setDefaultExecutor((sender, _) -> {
            if (sender.hasPermission("cytosis.commands.tps")) {
                double tps = Math.min(ServerFlag.SERVER_TICKS_PER_SECOND, Math.floor(1000 / tickMonitor.getTickTime()));
                sender.sendMessage(MM."<YELLOW>The current server TPS is \{tps}.");
            }
        });
    }
}
