package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;

public class SnooperUnmuteCommand extends CytosisCommand {
    public SnooperUnmuteCommand() {
        super("unmute");
        setCondition(CommandUtils.IS_STAFF);

        setDefaultExecutor((s, c) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            if (!player.getPreference(CytosisNamespaces.MUTE_SNOOPER)) {
                player.sendMessage(Msg.whoops("Snooper is not muted!"));
                return;
            }
            player.updatePreference(CytosisNamespaces.MUTE_SNOOPER, false);
            player.sendMessage(Msg.whoops("Snooper has been unmuted."));
        });
    }
}
