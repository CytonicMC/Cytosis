package net.cytonic.cytosis.commands;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.monitoring.TickMonitor;

import java.util.concurrent.atomic.AtomicReference;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the tps command
 */
public class TPSCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    @Getter private static AtomicReference<TickMonitor> lastTick = new AtomicReference<>();
    public TPSCommand() {
        super("tps");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.tps"));
        setDefaultExecutor((sender, _) -> {
            if (sender.hasPermission("cytosis.commands.tps")) {
                TickMonitor monitor = lastTick.get();
                double tickMs = monitor.getTickTime();
                double tps = Math.min(MinecraftServer.TICK_PER_SECOND, Math.floor(1000 / tickMs));
                sender.sendMessage(MM."<YELLOW>The current server TPS is \{tps}.");
            }
        });
    }
}
