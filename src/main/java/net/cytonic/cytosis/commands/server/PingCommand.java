package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;

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
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final CytosisPlayer player) {
                player.sendMessage(MM."<yellow><b>Pong!</b></yellow> <gray>Your ping is \{player.getLatency()}ms.");
            } else {
                sender.sendMessage(MM."<red>Only players may execute this command!");
            }
        });
    }
}
