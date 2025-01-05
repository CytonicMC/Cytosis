package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.minestom.server.command.builder.Command;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

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
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                return;
            }
            if (Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                player.sendMessage(MM."<white><b>UNVANISHED!</white> <gray>Vanish is now disabled!");
                Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.VANISHED, false);
                Cytosis.getVanishManager().disableVanish(player);
                return;
            }
            player.sendMessage(MM."<dark_gray><b>VANISHED!</dark_gray> <gray>Vanish is now enabled!");
            Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.VANISHED, true);
            Cytosis.getVanishManager().enableVanish(player);
        });
    }
}
