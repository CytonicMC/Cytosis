package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;

public class SnooperMuteCommand extends Command {

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
            player.sendMessage(Msg.mm("<b><red>MUTED!</red></b><gray> Snooper has been muted."));
        });
    }
}
