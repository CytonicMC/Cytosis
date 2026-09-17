package net.cytonic.cytosis.sidebar;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.sidebar.packet.SidebarController;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public interface PlayerSidebarViewer<V extends Player & SidebarViewer<V>> extends SidebarViewer<V> {

    @NotNull SidebarController<V> getSidebarController();

    @NotNull HashSet<SidebarComponent<V>> getVisibleComponents();

    @Override
    default boolean isViewingComponent(@NotNull SidebarComponent<V> component) {
        return this.getVisibleComponents().contains(component);
    }

    @Override
    default boolean canFitComponent(@NotNull SidebarComponent<V> component) {
        return this.getSidebarController().getFurthestLine() + component.getComponentLength() < SidebarController.SIDEBAR_LINE_SIZE;
    }

    @Override
    default boolean forceDisplayComponent(@NotNull SidebarComponent<V> component) {
        if (this.isViewingComponent(component) || !this.canFitComponent(component)) {
            return false;
        }

        assert this.getCurrentSidebar() != null;

        int startAbsoluteID = this.getCurrentSidebar().getStartAbsoluteIDComponent(component, (V) this);

        // First, we displace everything below to make space
        this.getSidebarController().displaceAllBelow(startAbsoluteID, component.getComponentLength());

        int componentStartScore = this.getSidebarController().getPreviousAbsoluteIDOffset(startAbsoluteID) + 1;

        // Then, we can add the component
        this.getSidebarController().addComponent(startAbsoluteID, componentStartScore, component.getContents((V) this));

        // Then finally update the viewed component list
        this.getVisibleComponents().add(component);

        return true;
    }

    @Override
    default boolean hideComponent(@NotNull SidebarComponent<V> component) {
        if (!this.isViewingComponent(component)) {
            return false;
        }

        assert this.getCurrentSidebar() != null;

        int startAbsoluteID = this.getCurrentSidebar().getStartAbsoluteIDComponent(component, (V) this);
        int endAbsoluteID = startAbsoluteID + component.getComponentLength();

        // First, we remove the component
        this.getSidebarController().removeComponent(startAbsoluteID, component.getComponentLength());

        // Then, we move the below lines for them to be of a continuous score
        this.getSidebarController().displaceAllBelow(startAbsoluteID, -component.getComponentLength());

        // Then finally update the viewed component list
        this.getVisibleComponents().remove(component);

        return true;
    }

    @Override
    default void updateComponent(@NotNull SidebarComponent<V> component) {
        if (!this.isViewingComponent(component)) {
            return;
        }

        assert this.getCurrentSidebar() != null;

        int startAbsoluteID = this.getCurrentSidebar().getStartAbsoluteIDComponent(component, (V) this);
        int endAbsoluteID = startAbsoluteID + component.getComponentLength();

        Collection<Component> componentContents = component.getContents((V) this);
        assert componentContents != null; // Is true because we assume the player can view the component.

        int i = 0;
        for (Component c : componentContents) {
            this.getSidebarController().changeLineText(startAbsoluteID + i, c);

            ++i;
        }
    }

    @Override
    default void updateSidebarTitle() {
        assert this.getCurrentSidebar() != null;

        this.getSidebarController().renameSidebar(this.getCurrentSidebar().getTitle((V) this));
    }
}
