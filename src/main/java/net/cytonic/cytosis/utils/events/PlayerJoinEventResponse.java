package net.cytonic.cytosis.utils.events;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

/**
 * The functional interface providing a bit more information about a player join.
 */
@FunctionalInterface
public interface PlayerJoinEventResponse {

    /**
     * Complete this interface
     *
     * @param player   The player
     * @param instance The instance the player is spawning in
     */
    void accept(Player player, Instance instance);
}
