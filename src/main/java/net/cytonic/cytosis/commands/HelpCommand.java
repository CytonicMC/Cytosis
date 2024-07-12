package net.cytonic.cytosis.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import static net.cytonic.utils.MiniMessageTemplate.MM;


/**
 * The class representing the help command
 */
public class HelpCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public HelpCommand() {
        super("help", "?");
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final Player player) {
                player.sendMessage(MM."<dark_green><st>                            </st> <gold>[<aqua>!</aqua>] <aqua><b>CytonicMC</b></aqua> [<aqua>!</aqua>]</gold> <st>                           </st>");
                player.sendMessage(" ");
                player.sendMessage(MM."<white><b>\u25C6</b></white> <gold><hover:show_text:'<gold>Click to join our discord!'><click:open_url:'https://discord.gg/yrmv9WSGGV'>Discord</click>");
                player.sendMessage(MM."<white><b>\u25C6</b></white> <gold><hover:show_text:'<gold>Click to open our forums!'><click:open_url:'https://cytonic.net/forums'>Forums</click>");
                player.sendMessage(MM."<white><b>\u25C6</b></white> <gold><hover:show_text:'<gold>Click to go to appeals!'><click:open_url:'https://cytonic.net/forums/topics/PUNISHMENT_APPEALS'>Appeals</click>");
                player.sendMessage(MM."<white><b>\u25C6</b></white> <gold><hover:show_text:'<gold>Click to go to bug reports!'><click:open_url:'https://cytonic.net/forums/topics/BUG_REPORTS'>Bug Reports</click>");
                player.sendMessage(" ");
                player.sendMessage(MM."<aqua>Don't be afraid to ask a staff member for help either in-game, on our forums, or on discord.");
                player.sendMessage(MM."<st><dark_green>                                                                                ");
            } else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
    }
}
