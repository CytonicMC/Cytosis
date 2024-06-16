package net.cytonic.cytosis.playerlist;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

/**
 * A class representing one line in the player list
 */
@Getter
@Setter
public class PlayerListEntry {
    private Component name;
    private PlayerListFavicon favicon;
    private int priority;

    /**
     * Creates a new player list entry
     *
     * @param name     The name of the entry
     * @param favicon  The {@link PlayerListFavicon} of the entry
     * @param priority The ordering of the entry
     */
    public PlayerListEntry(Component name, PlayerListFavicon favicon, int priority) {
        this.name = name;
        this.favicon = favicon;
        this.priority = priority;
    }

    /**
     * Creates a new player list entry with the favicon set to {@link PlayerListFavicon#GREY}
     * @param name The name of the entry
     * @param priority The ordering of the entry
     */
    public PlayerListEntry(Component name, int priority) {
        this.name = name;
        this.favicon = PlayerListFavicon.GREY;
        this.priority = priority;
    }

    /**
     * Creates a new player list entry with the favicon set to {@link PlayerListFavicon#GREY}
     * @param name The name of the entry
     * @param priority The ordering of the entry
     */
    public PlayerListEntry(String name, int priority) {
        this.name = Component.text(name);
        this.favicon = PlayerListFavicon.GREY;
        this.priority = priority;
    }

    /**
     * Creates a new player list entry
     * @param name The name of the entry
     * @param favicon The {@link PlayerListFavicon} of the entry
     * @param priority The ordering of the entry
     */
    public PlayerListEntry(String name, PlayerListFavicon favicon, int priority) {
        this.name = Component.text(name);
        this.favicon = favicon;
        this.priority = priority;
    }
}
