package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface ActionbarSupplier {
    ActionbarSupplier EMPTY = player -> Component.text("");
    ActionbarSupplier DEFAULT = player -> {
        boolean changed = false;
        Component component = Msg.mm("<white>You are currently ");

        if (player.isVanished()) {
            changed = true;
            component = component.append(Msg.mm("<red>VANISHED</red>"));
        }

        component = component.append(Msg.mm("."));

        return changed ? component : Component.empty();
    };

    Component getActionbar(CytosisPlayer player);
}
