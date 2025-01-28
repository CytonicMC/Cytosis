package net.cytonic.cytosis.playerlist;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;

import java.util.List;

/**
 * A creator for the playerlist, for use with {@link net.cytonic.cytosis.managers.PlayerListManager}
 */
public interface PlayerlistCreator {

    /**
     * Creates all the categories for the playerlist
     *
     * @return The list of categories
     * @param player The player to create {@link Column}s for
     */
    List<Column> createColumns(CytosisPlayer player);

    /**
     * Creates the header for the playerlist
     *
     * @return The header in Component form
     * @param player The player to create the header for
     */
    Component header(CytosisPlayer player);

    /**
     * creates the footer for the playerlist
     *
     * @return The footer in Component form
     * @param player The player to create the footer for
     */
    Component footer(CytosisPlayer player);

    /**
     * Gets the number of columns, between one and 4, inclusive.
     *
     * @return A number between 1 and 4  [1, 4]
     */
    int getColumnCount();
}
