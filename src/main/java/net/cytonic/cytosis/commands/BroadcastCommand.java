package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.Arrays;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class BroadcastCommand extends Command {

    public BroadcastCommand() {
        super("broadcast");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.broadcast"));
        var broadcastArgument = ArgumentType.StringArray("broadcastArgument");
        setDefaultExecutor((sender, _) -> {
            if (sender.hasPermission("cytosis.commands.broadcast")) {
                sender.sendMessage(MM."<RED>/broadcast (message)");
            }
        });
        addSyntax((sender, context) -> {
            if (sender.hasPermission("cytonic.commands.broadcast")) {
                if (Cytosis.getOnlinePlayers().size() > 0) {
                    for (Player online : Cytosis.getOnlinePlayers()) {
                        online.sendMessage(MM."<aqua><b>Broadcast</b></aqua><gray>»</gray> <white>\{String.join(" ", context.get(broadcastArgument))}");
                    }
                }
                sender.sendMessage(MM."<aqua><b>Broadcast</b></aqua><gray>»</gray> <white>\{String.join(" ", context.get(broadcastArgument))}");
            }
        }, broadcastArgument);
    }
}
