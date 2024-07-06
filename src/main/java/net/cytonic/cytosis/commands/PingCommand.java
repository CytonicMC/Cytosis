package net.cytonic.cytosis.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import static net.cytonic.utils.MiniMessageTemplate.MM;


/**
 * The class representing the ping command
 */
public class PingCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public PingCommand() {
        super("ping");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.ping"));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final Player player) {
                if (player.hasPermission("cytosis.commands.ping")) {
                    player.sendMessage(MM."<YELLOW>Pong! You're ping is \{player.getLatency()}MS.");
                }
            } else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
    }
}
