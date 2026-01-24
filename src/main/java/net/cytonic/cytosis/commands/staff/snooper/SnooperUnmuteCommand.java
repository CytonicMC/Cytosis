package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

public class SnooperUnmuteCommand extends CytosisCommand {

    public SnooperUnmuteCommand() {
        super("unmute");
        setCondition(CommandUtils.IS_STAFF);

        setDefaultExecutor((s, _) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            if (!player.getPreference(Preferences.MUTE_SNOOPER)) {
                player.sendMessage(Msg.whoops("Snooper is not muted!"));
                return;
            }
            player.updatePreference(Preferences.MUTE_SNOOPER, false);
            player.sendMessage(Msg.whoops("Snooper has been unmuted."));
        });
    }
}
