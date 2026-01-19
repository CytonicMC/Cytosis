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
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;

public class PlayerHolograms {

    public static final double SPACE = 0.28;

    public static final Map<Hologram, List<HologramEntity>> holograms = new HashMap<>();

    /**
     * @param entity The entity to add the bottom hologram as passenger
     */
    public static void addHologram(Hologram hologram, @Nullable Entity entity) {
        List<HologramEntity> entities = new ArrayList<>();
        Instance instance =
            hologram.getInstance() != null ? hologram.getInstance() : Cytosis.get(InstanceContainer.class);
        HologramEntity firstEntity = null;
        for (int i = 0; i < hologram.lines.size(); i++) {
            double y = 0.20d + ((hologram.lines.size() - 1 - i) * SPACE);
            HologramEntity hologramEntity = new HologramEntity(hologram.lines.get(i));
            hologramEntity.setInstance(instance, hologram.pos);
            hologramEntity.editEntityMeta(TextDisplayMeta.class, meta -> meta.setTranslation(new Vec(0, y, 0)));
            hologramEntity.addViewer(hologram.player);
            entities.add(hologramEntity);

            if (entity != null && firstEntity == null) {
                firstEntity = hologramEntity;
                entity.addPassenger(firstEntity);
                continue;
            }
            if (firstEntity != null) {
                firstEntity.addPassenger(hologramEntity);
            }
        }
        holograms.put(hologram, entities);
    }

    public static void removePlayer(CytosisPlayer player) {
        holograms.entrySet().removeIf(entry -> entry.getKey().getPlayer().equals(player));
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
