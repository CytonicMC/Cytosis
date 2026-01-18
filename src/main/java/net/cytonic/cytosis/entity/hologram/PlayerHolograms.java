package net.cytonic.cytosis.entity.hologram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;

public class PlayerHolograms {

    public static final Map<Hologram, List<HologramEntity>> holograms = new HashMap<>();

    public static void addHologram(Hologram hologram) {
        List<HologramEntity> entities = new ArrayList<>();
        double spacing = hologram.getSpacing();
        double startY = hologram.lines.size() * spacing - spacing;
        for (int i = 0; i < hologram.lines.size(); i++) {
            HologramEntity entity = new HologramEntity(hologram.lines.get(i));
            entity.setInstance(
                hologram.getInstance() != null ? hologram.getInstance() : Cytosis.get(InstanceContainer.class),
                hologram.pos.add(0, startY - (i * spacing), 0));
            entity.addViewer(hologram.player);
            entities.add(entity);
        }

        holograms.put(hologram, entities);
    }

    @Builder
    @Getter
    public static class Hologram {

        private final CytosisPlayer player;
        private final Pos pos;
        private final List<Component> lines;
        private final Instance instance;
        private final Function<CytosisPlayer, List<Component>> displayFunction;
        @Builder.Default
        private final double spacing = 0.3;
    }
}
