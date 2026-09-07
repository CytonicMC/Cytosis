package net.cytonic.cytosis.sidebar;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;

import java.util.Collection;

/**
 * A component inside a sidebar.
 * Basically represents one or multiple lines inside a sidebar that are related.
 * <p>
 * There are a few characteristics for a component:
 * - A component cannot change its length. This is intentional to allow for fast updates.
 * - A component can be either updated by a scheduler or manually updated when the contents change.
 * - A component can be shown or hidden to individual players.
 * - A component is part of a sidebar.
 * @param <P> The player type
 */
public interface SidebarComponent<P extends CytosisPlayer> {

    /**
     * Checks if the given player can display the component.
     * This is only checked whether the component is about to be displayed to player. Not on each update.
     * Meaning, you can use {@link #canDisplay} to determine if a part of a scoreboard will be shown to the player.
     *
     * @param player The player.
     */
    boolean canDisplay(P player);

    /**
     * Builds the contents of the sidebar component based on the player.
     * This will output a collection of length {@link #getComponentLength()} and that should be assumed.
     *
     * @param player The player.
     */
    Collection<Component> getContents(P player);

    /**
     * Get the length of the component in lines.
     * This should not change but may be allowed in the future.
     */
    int getComponentLength();

}
