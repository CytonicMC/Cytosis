package net.cytonic.cytosis.commands.defaultMinecraft;

import net.cytonic.cytosis.utils.Utils;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * A command for teleporting :)
 */
public class TeleportCommand extends Command {

    /**
     * A command for teleporting to a player, exact location, or relative block position
     */
    public TeleportCommand() {
        super("teleport", "tp");
        setCondition((source, _) -> source.hasPermission("cytosis.commands.teleport"));

        var entityArgument = ArgumentType.Entity("entity").singleEntity(true).onlyPlayers(true);
//        entityArgument.setSuggestionCallback((sender, _, suggestion) -> {
//            if (!(sender instanceof Player player && player.hasPermission("cytosis.commands.teleport"))) return;
//            Cytosis.getOnlinePlayers().forEach(entity -> suggestion.addEntry(new SuggestionEntry(entity.getUsername())));
//        });

        var positionArgument = ArgumentType.RelativeBlockPosition("position");

        var xArg = ArgumentType.Double("x");
        var yArg = ArgumentType.Double("y");
        var zArg = ArgumentType.Double("z");
        var yawArg = ArgumentType.Float("yaw").setDefaultValue(-181.0f); // intentionally invalid
        var pitchArg = ArgumentType.Float("pitch").setDefaultValue(-91.0f); // intentionally invalid

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                float yaw = context.get(yawArg) == -181.0F ? player.getPosition().yaw() : context.get(yawArg);
                float pitch = context.get(pitchArg) == -91.0F ? player.getPosition().pitch() : context.get(pitchArg);
                Pos p = new Pos(context.get(xArg), context.get(yArg), context.get(zArg), yaw, pitch);
                player.teleport(p);
                player.sendMessage(MM."<aqua><b>Teleported!</b></aqua> <dark_gray>-»<dark_gray> <gray>(\{Utils.TWO_PLACES.format(p.x())}, \{Utils.TWO_PLACES.format(p.y())}, \{Utils.TWO_PLACES.format(p.z())})");
            } else {
                sender.sendMessage(MM."Only players can use this command");
            }
        }, xArg, yArg, zArg, yawArg, pitchArg);

        setDefaultExecutor((source, _) -> source.sendMessage(MM."<red>Usage: /tp <x> <y> <z> [yaw] [pitch] | /tp <player> | /tp <relative>"));

        // entity arg
        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                EntityFinder finder = context.get(entityArgument);
                Entity entity = finder.findFirstEntity(sender);
                if (entity == null) {
                    player.sendMessage(MM."<red>Failed to find player.");
                    return;
                }
                player.teleport(entity.getPosition());
                player.sendMessage(MM."<green>Teleported!");
            } else {
                sender.sendMessage(MM."Only players can use this command");
            }
        }, entityArgument);

        // relative
        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                Pos p = context.get(positionArgument).from(player).asPosition();

                player.teleport(p.withPitch(player.getPosition().pitch()).withYaw(player.getPosition().yaw()));
                player.sendMessage(MM."<aqua><b>Teleported!</b></aqua> <dark_gray>-»<dark_gray> <gray>(\{Utils.TWO_PLACES.format(p.x())}, \{Utils.TWO_PLACES.format(p.y())}, \{Utils.TWO_PLACES.format(p.z())})");

            } else {
                sender.sendMessage(MM."Only players can use this command");
            }
        }, positionArgument);
    }

}
