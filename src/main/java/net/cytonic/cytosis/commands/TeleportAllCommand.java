package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the tpall command
 */
public class TeleportAllCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public TeleportAllCommand() {
        super("tpall");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.tpall"));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final Player player) {
                if (player.hasPermission("cytosis.commands.tpall")) {
                    for (Player online : Cytosis.getOnlinePlayers()) online.teleport(player.getPosition());
                    player.sendMessage(MM."<GREEN>Teleported everyone.");
                }
            } else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
    }
}
