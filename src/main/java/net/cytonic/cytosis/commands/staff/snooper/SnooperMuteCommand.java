package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;

public class SnooperMuteCommand extends CytosisCommand {

    public SnooperMuteCommand() {
        super("mute");
        setCondition(CommandUtils.IS_STAFF);

        setDefaultExecutor((s, c) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            if (player.getPreference(CytosisNamespaces.MUTE_SNOOPER)) {
                player.sendMessage(Msg.whoops("You have already muted snooper!"));
                return;
            }
            player.updatePreference(CytosisNamespaces.MUTE_SNOOPER, true);
            player.sendMessage(Msg.redSplash("MUTED!", "Snooper has been muted."));
        });
    }
}
