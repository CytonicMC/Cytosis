package net.cytonic.cytosis.playerlist;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.List;

public interface PlayerlistCreator {

    /**
     * Creates all the categories for the playerlist
     *
     * @return The list of categories
     */
    List<Column> createColumns(Player player);

    /**
     * Creates the header for the playerlist
     *
     * @return The header in Component form
     */
    Component header(Player player);

    /**
     * creates the footer for the playerlist
     *
     * @return The footer in Component form
     */
    Component footer(Player player);

    /**
     * Gets the number of columns, between one and 4, inclusive.
     *
     * @return A number between 1 and 4  [1, 4]
     */
    int getColumnCount();
}
