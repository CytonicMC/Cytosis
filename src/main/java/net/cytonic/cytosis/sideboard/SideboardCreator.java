package net.cytonic.cytosis.sideboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.List;

/**
 * An interface for creating sideboards.
 */
public interface SideboardCreator {
    /**
     * Creates the sideboard for the player
     *
     * @param player The player to create the sideboard for
     * @return The Sideboard for the player
     */
    Sideboard sideboard(Player player);

    /**
     * A method to create the lines for the sideboard
     * @param player The player
     * @return The list of components
     */
    List<Component> lines(Player player);

    /**
     * Creates the title for the sideboard
     * @param player The player
     * @return The title in Component form
     */
    Component title(Player player);
}
