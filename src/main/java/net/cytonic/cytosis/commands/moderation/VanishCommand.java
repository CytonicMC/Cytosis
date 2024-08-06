package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.CytosisPreferences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.NamespaceID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class VanishCommand extends Command {

    public static final List<UUID> vanishedPlayers = new ArrayList<>();

    public VanishCommand() {
        super("vanish");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.vanish"));
        setDefaultExecutor((sender, _) -> {
            if (sender instanceof Player player) {
                if (player.hasPermission("cytosis.commands.vanish")) {
                    if (!Cytosis.getPreferenceManager().getPlayerPreference(player.getUuid(), CytosisPreferences.VANISHED)) {
                        player.sendMessage(MM."<GREEN>Vanish is now enabled!");
                        Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), NamespaceID.from("cytosis:vanished"), true);
                        enableVanish(player);
                    } else {
                        player.sendMessage(MM."<RED>Vanish is now disabled!");
                        Cytosis.getPreferenceManager().updatePlayerPreference(player.getUuid(), NamespaceID.from("cytosis:vanished"), false);
                        disableVanish(player);
                    }
                }
            }
        });
    }

    public static void enableVanish(Player player) {
        player.setAutoViewable(false);
        for (Player onlinePlayer : Cytosis.getOnlinePlayers()) {
            onlinePlayer.removeViewer(player);
        }
        vanishedPlayers.add(player.getUuid());
    }

    public static void disableVanish(Player player) {
        player.setAutoViewable(true);
        for (Player onlinePlayer : Cytosis.getOnlinePlayers()) {
            onlinePlayer.addViewer(player);
        }
        vanishedPlayers.remove(player.getUuid());
    }
}
