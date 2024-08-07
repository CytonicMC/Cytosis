package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.entity.Player;

/**
 * This class handles vanishing
 */
public class VanishManager {

    /**
     * A default constructor for VanishManager
     */
    public VanishManager() {

    }

    /**
     * Enables vanish for a player
     *
     * @param player the player to vanish
     */
    public void enableVanish(Player player) {
        player.setAutoViewable(false);
        for (Player onlinePlayer : Cytosis.getOnlinePlayers()) {
            onlinePlayer.removeViewer(player);
        }
    }

    /**
     * Disables vanish a player
     *
     * @param player the player to unvanish
     */
    public void disableVanish(Player player) {
        player.setAutoViewable(true);
        for (Player onlinePlayer : Cytosis.getOnlinePlayers()) {
            onlinePlayer.addViewer(player);
        }
    }
}
