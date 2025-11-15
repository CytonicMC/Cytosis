package net.cytonic.cytosis.commands.defaultMinecraft;

import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.coordinate.Pos;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

/**
 * A command for teleporting :)
 */
public class TeleportCommand extends CytosisCommand {

    public static final ArgumentRelativeBlockPosition POS_ARG = ArgumentType.RelativeBlockPosition("position");

    private static final CommandExecutor RELATIVE_EXECUTOR = (sender, context) -> {
        if (sender instanceof CytosisPlayer player) {
            Pos p = context.get(POS_ARG).from(player).asPos();
            player.teleport(p.withPitch(player.getPosition().pitch()).withYaw(player.getPosition().yaw()));
            player.sendMessage(Msg.aquaSplash("TELEPORTED!",
                "<dark_gray>-»<dark_gray> <gray>(" + Utils.TWO_PLACES.format(p.x()) + ", "
                    + Utils.TWO_PLACES.format(p.y()) + ", " + Utils.TWO_PLACES.format(p.z())));

        }
    };

    private static final CommandExecutor PLAYER_EXECUTOR = (sender, context) -> {
        if (sender instanceof CytosisPlayer player) {
            CytosisPlayer target = context.get(CommandUtils.ONLINE_PLAYERS);
            if (target == null) {
                player.sendMessage(Msg.whoops("Player not found!"));
                return;
            }
            player.teleport(target.getPosition());
            player.sendMessage(Msg.green("Teleported!"));
        }
    };

    /**
     * A command for teleporting to a player, exact location, or relative block position
     */
    public TeleportCommand() {
        super("teleport", "tp");
        setCondition(CommandUtils.IS_STAFF);

        ArgumentDouble xArg = ArgumentType.Double("x");
        ArgumentDouble yArg = ArgumentType.Double("y");
        ArgumentDouble zArg = ArgumentType.Double("z");
        ArgumentFloat yawArg = ArgumentType.Float("yaw");
        yawArg.setDefaultValue(-181.0f); // intentionally invalid
        ArgumentFloat pitchArg = ArgumentType.Float("pitch");
        pitchArg.setDefaultValue(-91.0f); // intentionally invalid

        addSyntax((sender, context) -> {
            if (sender instanceof CytosisPlayer player) {
                float yaw = context.get(yawArg) == -181.0F ? player.getPosition().yaw() : context.get(yawArg);
                float pitch = context.get(pitchArg) == -91.0F ? player.getPosition().pitch() : context.get(pitchArg);
                Pos p = new Pos(context.get(xArg), context.get(yArg), context.get(zArg), yaw, pitch);
                player.teleport(p);
                player.sendMessage(Msg.aquaSplash("TELEPORTED!",
                    "<dark_gray>-»<dark_gray> <gray>(" + Utils.TWO_PLACES.format(p.x()) + ", "
                        + Utils.TWO_PLACES.format(p.y()) + ", " + Utils.TWO_PLACES.format(p.z()) + ")"));
            }
        }, xArg, yArg, zArg, yawArg, pitchArg);

        setDefaultExecutor((source, cmdc) -> source.sendMessage(
            Msg.red("Usage: /tp <x> <y> <z> [yaw] [pitch] | /tp <player> | /tp <relative>")));

        // entity arg
        addSyntax(PLAYER_EXECUTOR, CommandUtils.ONLINE_PLAYERS);

        // relative
        addSyntax(RELATIVE_EXECUTOR, POS_ARG);
    }

}
