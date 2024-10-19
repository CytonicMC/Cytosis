package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.minestom.server.command.builder.Command;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * The class representing the vanish command
 */
public class VanishCommand extends Command {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public VanishCommand() {
        super("vanish");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.vanish"));
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                return;
            }
            if (!player.hasPermission("cytosis.commands.vanish")) {
                return;
            }
            if (Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                player.sendMessage(MM."<green><b>UNVANISHED!</green> <gray>Vanish is now disabled!");
                Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.VANISHED, false);
                Cytosis.getVanishManager().disableVanish(player);
                return;
            }
            player.sendMessage(MM."<green><b>VANISHED!</green> <gray>Vanish is now enabled!");
            Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), CytosisNamespaces.VANISHED, true);
            Cytosis.getVanishManager().enableVanish(player);
        });
    }
}
