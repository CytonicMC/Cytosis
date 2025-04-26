package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface ActionbarSupplier {
    ActionbarSupplier EMPTY = player -> Component.empty();
    ActionbarSupplier DEFAULT = player -> {
        List<Component> statuses = new ArrayList<>();

        if (player.isVanished()) statuses.add(Msg.mm("<red>VANISHED</red>"));
        if (player.isNicked()) statuses.add(Msg.mm("<red>NICKED</red>"));

        if (statuses.isEmpty()) return Component.empty();

        return Msg.mm("<white>You are currently ")
                .append(Component.join(JoinConfiguration.builder()
                        .separator(Msg.mm(", "))
                        .lastSeparator(Msg.mm(" & ")).build(), statuses))
                .append(Msg.mm("."));
    };

    Component getActionbar(CytosisPlayer player);
}
