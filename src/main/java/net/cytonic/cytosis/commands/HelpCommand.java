package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.entity.Player;

/**
 * The class representing the help command
 */
public class HelpCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public HelpCommand() {
        super("help", "?");
        setDefaultExecutor((sender, ignored) -> {
            if (sender instanceof final Player player) {
                player.sendMessage(Msg.mm("<dark_green><st>                            </st> <gold>[<aqua>!</aqua>] <aqua><b>CytonicMC</b></aqua> [<aqua>!</aqua>]</gold> <st>                           </st>"));
                player.sendMessage(" ");
                player.sendMessage(Msg.mm("<white><b>\u25C6</b></white> <gold><hover:show_text:'<gold>Click to join our discord!'><click:open_url:'https://discord.gg/yrmv9WSGGV'>Discord</click>"));
                player.sendMessage(Msg.mm("<white><b>\u25C6</b></white> <gold><hover:show_text:'<gold>Click to open our forums!'><click:open_url:'https://cytonic.net/forums'>Forums</click>"));
                player.sendMessage(Msg.mm("<white><b>\u25C6</b></white> <gold><hover:show_text:'<gold>Click to go to appeals!'><click:open_url:'https://cytonic.net/forums/topics/PUNISHMENT_APPEALS'>Appeals</click>"));
                player.sendMessage(Msg.mm("<white><b>\u25C6</b></white> <gold><hover:show_text:'<gold>Click to go to bug reports!'><click:open_url:'https://cytonic.net/forums/topics/BUG_REPORTS'>Bug Reports</click>"));
                player.sendMessage(" ");
                player.sendMessage(Msg.mm("<aqua>Don't be afraid to ask a staff member for help either in-game, on our forums, or on discord."));
                player.sendMessage(Msg.mm("<st><dark_green>                                                                                "));
            } else {
                sender.sendMessage(Msg.mm("<RED>Only players may execute this command!"));
            }
        });
    }
}
