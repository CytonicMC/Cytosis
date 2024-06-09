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

    public void sortEntries() {
        entries.sort(Comparator.comparingInt(PlayerListEntry::getPriority));
    }

    public PlayerListCategory(Component name, PlayerListFavicon favicon, int priority, List<PlayerListEntry> entries) {
        this.name = name;
        this.favicon = favicon;
        this.priority = priority;
        this.entries = entries;
    }
}
