package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.minestom.server.command.builder.Command;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class SnooperUnmuteCommand extends Command {
    public SnooperUnmuteCommand() {
        super("unmute");
        setCondition(CommandUtils.IS_STAFF);

        setDefaultExecutor((s, c) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            if (!player.getPreference(CytosisNamespaces.MUTE_SNOOPER)) {
                player.sendMessage(MM."<b><red>WHOOPS!</red></b><gray> Snooper is not muted!");
                return;
            }
            player.updatePreference(CytosisNamespaces.MUTE_SNOOPER, false);
            player.sendMessage(MM."<b><green>UNMUTED!</green></b><gray> Snooper has been unmuted.");
        });
    }
}
