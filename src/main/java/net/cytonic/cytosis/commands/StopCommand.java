package net.cytonic.cytosis.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

/**
 * A class representing the stop command
 */
public class StopCommand extends Command {

    /**
     * A simple command to stop the server
     */
    public StopCommand() {
        super("stop");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.stop"));
        addSyntax((sender, _) -> {
            if (sender.hasPermission("cytonic.commands.stop"))
                MinecraftServer.stopCleanly();
        });
    }
}
