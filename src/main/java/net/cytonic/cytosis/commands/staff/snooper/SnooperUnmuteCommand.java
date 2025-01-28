package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;

public class SnooperUnmuteCommand extends Command {
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
