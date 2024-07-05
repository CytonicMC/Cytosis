package net.cytonic.cytosis.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the fly command
 */
public class FlyCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public FlyCommand() {
        super("fly");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.fly"));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final Player player) {
                if (player.hasPermission("cytosis.commands.fly")) {
                    if (player.isAllowFlying() == false) {
                        player.setAllowFlying(true);
                        player.setFlying(true);
                        player.sendMessage(MM."<GREEN>Flight enabled.");
                    } else {
                        player.setAllowFlying(false);
                        player.setFlying(false);
                        player.sendMessage(MM."<RED>Flight disabled.");
                    }
                }
            } else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
    }
}
