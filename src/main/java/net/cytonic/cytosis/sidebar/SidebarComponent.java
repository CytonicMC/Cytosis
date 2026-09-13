package net.cytonic.cytosis.sidebar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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

    /**
     * Gets the "id" / name of the component. Is used internally only.
     */
    @Nullable String getId();

    /**
     * Checks if this component's content could ever change / be updated.
     */
    boolean isStatic();

    class Builder<V extends SidebarViewer<V>> {
        private final List<Component> contents;
        private final String id;

        public Builder(String id) {
            this.id = id;
            this.contents = new ArrayList<>();
        }

        public Builder<V> line(Component component) {
            this.contents.add(component);

            return this;
        }

        public SidebarComponent<V> build() {
            return new SidebarComponent<V>() {
                @Override
                public boolean canDisplay(@NotNull V viewer) {
                    return true;
                }

                @Override
                public Collection<Component> getContents(@NotNull V viewer) {
                    return contents;
                }

                @Override
                public int getComponentLength() {
                    return contents.size();
                }

                @Override
                public @Nullable String getId() {
                    return id;
                }

                @Override
                public boolean isStatic() {
                    return true;
                }
            };
        }
    }

    /**
     * A variant of builder that allows for dynamic lines.
     * It is highly not recommended to use this to construct actual
     * dynamic components.
     * @param <V>
     */
    class DynamicBuilder<V extends SidebarViewer<V>> {
        private final List<Function<V, Component>> contents;
        private final Function<V, Boolean> canDisplayInner;
        private final String id;

        public DynamicBuilder(String id, Function<V, Boolean> canDisplay) {
            this.id = id;
            this.canDisplayInner = canDisplay;
            this.contents = new ArrayList<>();
        }

        public DynamicBuilder<V> line(Component component) {
            this.contents.add((_) -> component);
            return this;
        }

        public DynamicBuilder<V> line(Function<V, Component> line) {
            this.contents.add(line);
            return this;
        }

        public SidebarComponent<V> build() {
            return new SidebarComponent<V>() {
                @Override
                public boolean canDisplay(@NonNull V viewer) {
                    return canDisplayInner.apply(viewer);
                }

                @Override
                public Collection<Component> getContents(@NonNull V viewer) {
                    List<Component> components = new ArrayList<>();

                    for(Function<V, Component> line : contents) {
                        components.add(line.apply(viewer));
                    }

                    return components;
                }

                @Override
                public int getComponentLength() {
                    return contents.size();
                }

                @Override
                public @Nullable String getId() {
                    return id;
                }

                @Override
                public boolean isStatic() {
                    return false;
                }
            };
        }
    }

}
