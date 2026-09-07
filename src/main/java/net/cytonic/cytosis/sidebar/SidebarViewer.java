package net.cytonic.cytosis.sidebar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a viewer of a sidebar.
 *
 * @param <V> the self viewer type.
 */
public interface SidebarViewer<V extends SidebarViewer<V>> {

    /**
     * Checks if the viewer is currently viewing the component.
     * This doesn't check if the viewer <b>can</b> view the component, just if the viewer is currently viewing it.
     * @param component The component.
     */
    boolean isViewingComponent(@NotNull SidebarComponent<V> component);

    /**
     * Display the component to the viewer if the viewer can view it.
     * This generally cannot fail due to the check to make sure that the viewer is supposed to be able to view it.
     * However, if you wish to display this component anyway, use {@link #forceDisplayComponent(SidebarComponent)}.
     * <p>
     * If the component is already being displayed, the function will fail and return false.
     * <p>
     * <h1>Update warnings</h1>
     * <p>This function <b>will</b> update the viewer's scoreboard if it doesn't fail.</p>
     * <p>Adding at the start of the scoreboard is usually the fastest scenario since no lines need to be moved.</p>
     * <p>This function may move other lines if needed using a `set_score` packet.</p>
     *
     * @param component The component to display.
     * @return the fail state of the display. If the component couldn't be displayed, return false, otherwise true.
     */
    boolean displayComponent(@NotNull SidebarComponent<V> component);

    /**
     * The "unsafe" variant of {@link #displayComponent(SidebarComponent)}.
     * Displays the component to viewer regardless of whether the viewer can view it or not.
     * <p>
     * If the component is already being displayed, the function will fail and return false.
     * <p>
     * <h1>Update warnings</h1>
     * <p>This function <b>will</b> update the viewer's scoreboard if it doesn't fail.</p>
     * <p>Adding at the end of the scoreboard is usually the fastest scenario since no lines need to be moved.</p>
     * <p>This function may move other lines if needed using a `set_score` packet.</p>
     *
     * @param component The component to display.
     * @return the fail state of the display. If the component couldn't be displayed, return false, otherwise true.
     */
    boolean forceDisplayComponent(@NotNull SidebarComponent<V> component);

    /**
     * Hides the given component to the viewer.
     * <p>
     * If the component is already hidden / not being displayed, the function will fail and return false.
     * <p>
     * <h1>Update warnings</h1>
     * <p>This function <b>will</b> update the viewer's scoreboard if it doesn't fail.</p>
     * <p>This function usually is pretty fast as it only requires resetting scores rather than moving them.</p>
     *
     * @param component The component to hide.
     * @return the fail state of the hide. If the component couldn't be hidden, return false, otherwise true.
     */
    boolean hideComponent(@NotNull SidebarComponent<V> component);

    /**
     * Updates the component to the viewer if they are viewing the component.
     * <p>
     * If the viewer isn't viewing the component, the function will fail and return false.
     * <p>
     * <h1>Update warnings</h1>
     * <p>This function <b>will</b> update the viewer's scoreboard if it doesn't fail.</p>
     *
     * @param component The component to update.
     */
    void updateComponent(@NotNull SidebarComponent<V> component);

    /**
     * Get the current sidebar the viewer is viewing.
     * @return The current sidebar or null if there's none.
     */
    @Nullable Sidebar<V> getCurrentSidebar();

    /**
     * Removes the current sidebar (if there's one) and displays the given sidebar.
     * If sidebar is null, then don't display anything.
     * <p></p>
     * @param sidebar The sidebar to display.
     */
    void setCurrentSidebar(@Nullable Sidebar<V> sidebar);

}
