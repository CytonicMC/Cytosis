package net.cytonic.cytosis.commands.staff;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

/**
 * The class representing the fly command
 */
public class FlyCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public FlyCommand() {
        super("fly");
        setCondition(CommandUtils.IS_STAFF);
        ArgumentFloat speedArg = ArgumentType.Float("speed");
        speedArg.between(0.1F, 5F);

        addSyntax((sender, ctx) -> {
            if (!(sender instanceof final CytosisPlayer player)) return;
            float speed = ctx.get(speedArg);
            execute(player, speed, true);
        }, speedArg);

        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof final CytosisPlayer player)) return;
            execute(player, player.getPreference(Preferences.FLY_SPEED), false);
        });
    }

    private void execute(CytosisPlayer player, float speed, boolean overwrite) {
        if (!player.getPreference(Preferences.FLY) || player.getPreference(Preferences.FLY_SPEED) != speed) {
            player.setAllowFlying(true);
            player.setFlying(true);
            player.setFlyingSpeed(speed * 0.05F);
            player.sendMessage(Msg.green("Flight enabled at %.2fx speed.", speed));
            player.updatePreference(Preferences.FLY, true);
            if (overwrite) {
                player.updatePreference(Preferences.FLY_SPEED, speed);
            }
        } else {
            player.setAllowFlying(false);
            player.setFlying(false);
            player.sendMessage(Msg.red("Flight disabled."));
            player.updatePreference(Preferences.FLY, false);
        }
    }
}
