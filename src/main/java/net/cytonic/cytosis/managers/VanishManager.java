package net.cytonic.cytosis.managers;

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
        player.updateViewableRule(p -> p.hasPermission("cytosis.vanish.can_see_vanished"));
        //todo events?
    }

    /**
     * Disables vanish a player
     *
     * @param player the player to unvanish
     */
    public void disableVanish(Player player) {
        player.updateViewableRule(_ -> true);
    }
}
