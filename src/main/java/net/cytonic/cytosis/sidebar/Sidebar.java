package net.cytonic.cytosis.sidebar;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/**
 * A sidebar inside the Cytosis sidebar API. Contains components, a title and a list of viewer.
 * @param <V> the type of viewer.
 */
public abstract class Sidebar<V extends SidebarViewer<V>> {

    /**
     * The offset to put something at the start (the bottom) of the sidebar.
     */
    public static final int SIDEBAR_POS_START = 0;

    /**
     * The offset to put something at the end (the top) of the sidebar.
     */
    public static final int SIDEBAR_POS_END = -1;

    @Getter
    private final HashSet<V> viewers;

    public Sidebar() {
        this.viewers = new HashSet<>();
    }

    /**
     * Get the title of the sidebar for the given viewer. This currently does not support updating.
     * @param viewer The viewer.
     */
    public abstract @NotNull Component getTitle(@NotNull V viewer);

    /**
     * Get the components of the sidebar from bottom to top.
     */
    public abstract @NotNull Collection<SidebarComponent<V>> getComponents();

    /**
     * Add a viewer to the sidebar. Doesn't really do anything.
     * @param viewer The viewer.
     */
    public void addViewer(V viewer) {
        this.viewers.add(viewer);
    }

    /**
     * Removes a viewer from the sidebar. Doesn't really do anything.
     * @param viewer The viewer.
     */
    public void removeViewer(V viewer) {
        this.viewers.remove(viewer);
    }

    public int getStartAbsoluteIDComponent(SidebarComponent<V> component, V viewer) {
        int offset = 0;

        for(SidebarComponent<V> c : this.getComponents()) {
            if(c == component) return offset;

            if(viewer.isViewingComponent(c)) {
                offset += c.getComponentLength();
            }
        }

        return offset;
    }

    /**
     * Utility class to build a {@link Sidebar}.
     * @param <V> the type of viewer.
     */
    public static class Builder<V extends SidebarViewer<V>> {
        private final List<SidebarComponent<V>> components;
        private final Function<V, Component> titleProvider;

        /**
         * Creates a new builder with the given provider for the title.
         * @param titleProvider the provider for the title.
         */
        public Builder(Function<V, Component> titleProvider) {
            this.titleProvider = titleProvider;
            this.components = new ArrayList<>();
        }

        /**
         * Adds a component to the sidebar.
         * @param component The component to add.
         */
        public Builder<V> component(SidebarComponent<V> component) {
            this.components.add(component);
            return this;
        }

        /**
         * Builds the sidebar based on the builder.
         * @return The built sidebar
         */
        public Sidebar<V> build() {
            return new Sidebar<V>() {
                @Override
                public @NotNull Component getTitle(@NonNull V viewer) {
                    return titleProvider.apply(viewer);
                }

                @Override
                public @NotNull Collection<SidebarComponent<V>> getComponents() {
                    return components;
                }
            };
        }

    }

}
