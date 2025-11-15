package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

/**
 * A debug command that tells the player they have operator status. (Useful for some mods like MoulberrysTweaks)
 */
public class OpMeCommand extends CytosisCommand {

    public OpMeCommand() {
        super("opme");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, ctx) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.setPermissionLevel(4);
            player.sendMessage(Msg.aquaSplash("OPPED!",
                "You've been upgraded to <dark_aqua>OPERATOR</dark_aqua>!"));
        });
    }

}
