package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * The class representing the broadcast command
 */
public class BroadcastCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public BroadcastCommand() {
        super("broadcast", "bc");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.broadcast"));
        var broadcastArgument = ArgumentType.StringArray("broadcastArgument");
        setDefaultExecutor((sender, _) -> {
            if (sender.hasPermission("cytosis.commands.broadcast")) {
                sender.sendMessage(MM."<RED>Usage: /broadcast (message)");
            }
        });
        addSyntax((sender, context) -> {
            if (sender.hasPermission("cytonic.commands.broadcast")) {
                if (!Cytosis.getOnlinePlayers().isEmpty()) {
                    for (Player online : Cytosis.getOnlinePlayers()) {
                        online.sendMessage(MM."<aqua><b>Broadcast</b></aqua> <gray>»</gray> <white>\{String.join(" ", context.get(broadcastArgument))}");
                    }
                }
            }
        }, broadcastArgument);
    }
}
