package net.cytonic.cytosis.playerlist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

/**
 * A class representing one column in the player list. Columns are used to display data in the player list, holding up
 * to 19 entries. If a column has more than 20 entries, they will be ignored.
 */
@Getter
@Setter
public class Column {

    private Component name;
    private PlayerListFavicon favicon;
    private List<PlayerListEntry> entries; // <PlayerListEntry>

    /**
     * Creates a new column with the specified name, favicon and entries
     *
     * @param name    the title of the column, shown at the top
     * @param favicon the favicon, or player head texture shown at on the top row, next to the name.
     * @param entries the entries in the column, with a max of 19. Entries over 19 are ignored
     */
    public Column(Component name, PlayerListFavicon favicon, List<PlayerListEntry> entries) {
        this.name = name;
        this.favicon = favicon;
        this.entries = entries;
    }

    /**
     * Creates a new column with the specified name and favicon. The entries are set to an empty list
     *
     * @param name    the title of the column, shown at the top.
     * @param favicon the favicon, or player head texture shown at on the top row, next to the name.
     */
    public Column(Component name, PlayerListFavicon favicon) {
        this.name = name;
        this.favicon = favicon;
        this.entries = new ArrayList<>();
    }

    /**
     * Sorts the entries by priority
     */
    public void sortEntries() {
        entries.sort(Comparator.comparingInt(PlayerListEntry::getPriority));
    }
}
