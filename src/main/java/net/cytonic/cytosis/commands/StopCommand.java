package net.cytonic.cytosis.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.stop"));
        addSyntax((sender, _) -> {
            if (sender.hasPermission("cytonic.commands.stop"))
                MinecraftServer.stopCleanly();
        });
    }
}
