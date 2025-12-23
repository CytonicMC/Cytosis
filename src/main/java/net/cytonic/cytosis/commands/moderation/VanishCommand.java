package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.VanishManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;

/**
 * The class representing the vanish command
 */
public class VanishCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public VanishCommand() {
        super("vanish");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, iognored) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            VanishManager vanishManager = Cytosis.get(VanishManager.class);
            PreferenceManager preferenceManager = Cytosis.get(PreferenceManager.class);

            if (preferenceManager.getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                player.sendMessage(Msg.splash("UNVANISHED!", "cec4c6", "Vanish is now disabled!"));
                preferenceManager
                    .updatePlayerPreference(player.getUuid(), CytosisNamespaces.VANISHED, false);
                vanishManager.disableVanish(player);
                return;
            }
            player.sendMessage(Msg.splash("VANISHED!", "787072", "Vanish is now enabled!"));
            preferenceManager.updatePlayerPreference(player.getUuid(), CytosisNamespaces.VANISHED, true);
            vanishManager.enableVanish(player);
        });
    }
}