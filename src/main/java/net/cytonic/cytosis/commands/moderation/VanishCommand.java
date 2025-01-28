package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;

/**
 * The class representing the vanish command
 */
public class VanishCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public VanishCommand() {
        super("vanish");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, iognored) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                return;
            }
            if (Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                player.sendMessage(Msg.mm("<#cec4c6><b>UNVANISHED!</#cec4c6> <gray>Vanish is now disabled!"));
                Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.VANISHED, false);
                Cytosis.getVanishManager().disableVanish(player);
                return;
            }
            player.sendMessage(Msg.mm("<#787072><b>VANISHED!</#787072> <gray>Vanish is now enabled!"));
            Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.VANISHED, true);
            Cytosis.getVanishManager().enableVanish(player);
        });
    }
}
