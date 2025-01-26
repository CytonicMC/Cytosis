package net.cytonic.cytosis.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

@FunctionalInterface
public interface ActionbarSupplier {
    ActionbarSupplier EMPTY = player -> Component.text("");

    Component getActionbar(Player player);
}
