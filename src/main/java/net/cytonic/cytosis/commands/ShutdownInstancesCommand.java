package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.minestom.server.command.builder.Command;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class ShutdownInstancesCommand extends Command {
    public ShutdownInstancesCommand() {
        super("shutdowninstances");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.shutdowninstances"));
        addSyntax((sender, _) -> {
            if (!sender.hasPermission("cytosis.commands.shutdowninstances")) {
                sender.sendMessage(MM."<RED>You do not have permission to use this command!");
                return;
            }
            if (!CytosisSettings.KUBERNETES_SUPPORTED) {
                sender.sendMessage(MM."<RED>This command is not supported on this server!");
                return;
            }
            Cytosis.getContainerizedInstanceManager().shutdownAllInstances();
            sender.sendMessage(MM."<GREEN>Shutting down Cytosis instances!");
        });
    }
}
