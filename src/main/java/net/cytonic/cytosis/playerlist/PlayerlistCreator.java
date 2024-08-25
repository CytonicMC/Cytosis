package net.cytonic.cytosis.playerlist;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

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
    List<Column> createColumns(Player player);

    /**
     * Creates the header for the playerlist
     *
     * @return The header in Component form
     * @param player The player to create the header for
     */
    Component header(Player player);

    /**
     * creates the footer for the playerlist
     *
     * @return The footer in Component form
     * @param player The player to create the footer for
     */
    Component footer(Player player);

    /**
     * Gets the number of columns, between one and 4, inclusive.
     *
     * @return A number between 1 and 4  [1, 4]
     */
    int getColumnCount();
}
