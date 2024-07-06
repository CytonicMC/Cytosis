package net.cytonic.cytosis.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the help command
 */
public class HelpCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public HelpCommand() {
        super("help");
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final Player player) {
                player.sendMessage(MM."<aqua><b>CytonicMC</b></aqua>");
                player.sendMessage(" ");
                player.sendMessage(MM."<gold>Forums: https://cytonic.net/forums");
                player.sendMessage(MM."<gold>Discord: https://discord.gg/yrmv9WSGGV");
                player.sendMessage(" ");
                player.sendMessage(MM."<green>Don't be afraid to ask a staff member for help");
                player.sendMessage(MM."<green>either in-game, on our forums, or on discord.");
            } else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
    }
}