package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.effects.Effects;
import net.minestom.server.entity.Player;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * The class representing the kaboom command
 */
public class KaboomCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public KaboomCommand() {
        super("kaboom");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.kaboom"));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof final Player player) {
                if (player.hasPermission("cytosis.commands.kaboom")) {
                    for (Player online : Cytosis.getOnlinePlayers()) {
                        online.sendMessage(MM."<GREEN>Kaboom!");
                        online.playEffect(Effects.ELECTRIC_SPARK, (int) online.getPosition().x(), (int) online.getPosition().y(), (int) online.getPosition().z(), 0, false);
                        online.setVelocity(new Vec(0, 1, 0));
                    }
                }
            } else {
                sender.sendMessage(MM."<RED>Only players may execute this command!");
            }
        });
    }
}