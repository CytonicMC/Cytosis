package net.cytonic.cytosis.playerlist;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.Comparator;
import java.util.List;

/**
 * A class that holds data about a category in the player list
 */
@Getter
@Setter
public class PlayerListCategory {
    private Component name;
    private PlayerListFavicon favicon;
    private int priority;
    private boolean enabled;
    private List<PlayerListEntry> entries; // <PlayerListEntry>

    /**
     * Sort the entries in the category by priority
     */
    public void sortEntries() {
        entries.sort(Comparator.comparingInt(PlayerListEntry::getPriority));
    }

    /**
     * Creates a new player list category
     *
     * @param name     The name of the category
     * @param favicon  The {@link PlayerListFavicon} of the category
     * @param priority The ordering of the category
     * @param entries  The {@link PlayerListEntry}s in the category
     */
    public PlayerListCategory(Component name, PlayerListFavicon favicon, int priority, List<PlayerListEntry> entries) {
        this.name = name;
        this.favicon = favicon;
        this.priority = priority;
        this.entries = entries;
    }
}
