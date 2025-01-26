package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;


/**
 * The class representing the ping command
 */
public class PingCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public PingCommand() {
        super("ping");
        setDefaultExecutor((sender, cmdc) -> {
            if (sender instanceof final CytosisPlayer player) {
                player.sendMessage(Msg.mm("<yellow><b>Pong!</b></yellow> <gray>Your ping is " + player.getLatency() + "ms."));
            } else {
                sender.sendMessage(Msg.mm("<red>Only players may execute this command!"));
            }
        });
    }
}
