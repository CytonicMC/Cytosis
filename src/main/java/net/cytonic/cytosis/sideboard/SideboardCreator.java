package net.cytonic.cytosis.sideboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.List;

/**
 * An interface for creating sideboards.
 */
public interface SideboardCreator {
    Sideboard sideboard(Player player);

    List<Component> lines(Player player);

    Component title(Player player);
}
