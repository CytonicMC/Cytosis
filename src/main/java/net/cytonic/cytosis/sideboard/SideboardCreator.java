package net.cytonic.cytosis.sideboard;

import java.util.List;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.player.CytosisPlayer;

/**
 * An interface for creating sideboards.
 */
public interface SideboardCreator<P extends CytosisPlayer> {

    /**
     * Creates the sideboard for the player
     *
     * @param player The player to create the sideboard for
     * @return The Sideboard for the player
     */
    Sideboard sideboard(P player);

    /**
     * A method to create the lines for the sideboard
     *
     * @param player The player
     * @return The list of components
     */
    List<Component> lines(P player);

    /**
     * Creates the title for the sideboard
     *
     * @param player The player
     * @return The title in Component form
     */
    Component title(P player);
}
