package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;


/**
 * The class representing the ping command
 */
public class PingCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public PingCommand() {
        super("ping");
        setDefaultExecutor((sender, cmdc) -> {
            if (sender instanceof final CytosisPlayer player) {
                player.sendMessage(Msg.yellowSplash("Pong!", "Your ping is " + player.getLatency() + "ms."));
            } else {
                sender.sendMessage(Msg.whoops("Only players may execute this command!"));
            }
        });
    }
}
