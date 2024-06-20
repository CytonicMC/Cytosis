package net.cytonic.cytosis.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class TeleportCommand extends Command {

    public TeleportCommand() {
        super("teleport", "tp");
        setCondition((source, _) -> source.hasPermission("cytosis.commands.teleport"));

        var entityArgument = ArgumentType.Entity("entity").singleEntity(true);
        var positionArgument = ArgumentType.RelativeBlockPosition("position");

        var xArg = ArgumentType.Double("x");
        var yArg = ArgumentType.Double("y");
        var zArg = ArgumentType.Double("z");
        var yawArg = ArgumentType.Float("yaw").setDefaultValue(0.0f);
        var pitchArg = ArgumentType.Float("pitch").setDefaultValue(0.0f);

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                Pos pos = new Pos(context.get(xArg), context.get(yArg), context.get(zArg),
                        context.get(yawArg), context.get(pitchArg));
                player.teleport(pos);
                player.sendMessage(MM."<green>Teleported to \{xArg} \{yArg} \{zArg} \{yawArg} \{pitchArg}");
            } else {
                sender.sendMessage(MM."Only players can use this command");
            }
        }, xArg, yArg, zArg, yawArg, pitchArg);

        setDefaultExecutor((source, _) -> source.sendMessage(MM."<red>Usage: /tp <player> <x> <y> <z> <yaw> <pitch> | /tp <entity>"));

        // entity arg
        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                Entity entity = context.get(entityArgument).findFirstEntity(sender);
                if (entity == null) {
                    player.sendMessage(MM."Failed to find entity.");
                    return;
                }
                player.teleport(entity.getPosition());
                player.sendMessage(MM."<green>Teleported to \{entity.getEntityType().name().toLowerCase()}");
            } else {
                sender.sendMessage(MM."Only players can use this command");
            }
        }, entityArgument);

        // relative
        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                Pos pos = player.getPosition().add(context.get(positionArgument).fromSender(player));
                player.teleport(pos);
                player.sendMessage(MM."<green>Teleported to \{pos.x()} \{pos.y()} \{pos.z()}");
            } else {
                sender.sendMessage(MM."Only players can use this command");
            }
        }, positionArgument);
    }

}
