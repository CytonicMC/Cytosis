package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.VanishManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

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
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            VanishManager vanishManager = Cytosis.get(VanishManager.class);
            player.togglePreference(Preferences.VANISHED, () -> {
                player.sendMessage(Msg.splash("VANISHED!", "787072", "Vanish is now enabled!"));
                vanishManager.enableVanish(player);
            }, () -> {
                player.sendMessage(Msg.splash("UNVANISHED!", "cec4c6", "Vanish is now disabled!"));
                vanishManager.disableVanish(player);
            });
        });
    }
}