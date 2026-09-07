package net.cytonic.cytosis.sidebar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 * @param <V> The viewer type
 *
 * This should be instantiated once per "component" and stored somewhere.
 */
public interface SidebarComponent<V extends SidebarViewer<V>> {

    /**
     * Checks if the given viewer can display the component.
     * This is only checked whether the component is about to be displayed to viewer. Not on each update.
     * Meaning, you can use {@link #canDisplay} to determine if a part of a scoreboard will be shown to the viewer.
     *
     * @param viewer The viewer.
     */
    boolean canDisplay(@NotNull V viewer);

    /**
     * Builds the contents of the sidebar component based on the viewer.
     * This will output a collection of length {@link #getComponentLength()} and that should be assumed.
     * The output collection can be null if the viewer isn't supposed to view the component, whether it is null or not in this case
     * is defined by how the component implementation of {@link #getContents(SidebarViewer)} is created.
     *
     * @param viewer The viewer.
     */
    @Nullable Collection<Component> getContents(@NotNull V viewer);

    /**
     * Get the length of the component in lines.
     * This should not change but may be allowed in the future.
     */
    int getComponentLength();

}
