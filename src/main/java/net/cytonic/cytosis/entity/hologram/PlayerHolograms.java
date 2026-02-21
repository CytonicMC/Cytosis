package net.cytonic.cytosis.entity.hologram;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.MetadataPacketBuilder;

public class PlayerHolograms {

    public static final double SPACE = 0.28;

    public static final List<Hologram> holograms = new CopyOnWriteArrayList<>();

    public static void addHologram(Hologram hologram) {
        List<Integer> entities = new ArrayList<>();
        for (int i = 0; i < hologram.lines.size(); i++) {
            double y = (hologram.lines.size() - 1 - i) * SPACE;
            int entityId = Entity.generateId();
            entities.add(entityId);

            hologram.player.sendPackets(
                new SpawnEntityPacket(entityId, UUID.randomUUID(), EntityType.TEXT_DISPLAY, hologram.pos.add(0, y, 0),
                    0, 0, Vec.ZERO),
                MetadataPacketBuilder.empty(entityId)
                    .setTextComponent(MetadataDef.TextDisplay.TEXT.index(), hologram.lines.get(i))
                    .setByte(MetadataDef.TextDisplay.BILLBOARD_CONSTRAINTS.index(), (byte) 3) // CENTER
                    .setBoolean(MetadataDef.TextDisplay.HAS_NO_GRAVITY.index(), true)
                    .setVarInt(MetadataDef.TextDisplay.LINE_WIDTH.index(), 1000)
                    .build()
            );
        }
        hologram.entityIds.addAll(entities);
        holograms.add(hologram);
    }

    public static void updateHologram(Hologram hologram, Pos pos) {
        List<Integer> entities = hologram.entityIds;
        if (entities != null) {
            for (int i = 0; i < hologram.lines.size(); i++) {
                double y = (hologram.lines.size() - 1 - i) * SPACE;
                int entityId = entities.get(i);
                hologram.player.sendPacket(new EntityTeleportPacket(entityId, pos.add(0, y, 0), Vec.ZERO, 0, false));
            }
        }
    }

    public static void removeHologram(Hologram hologram) {
        holograms.remove(hologram);
        List<Integer> entities = hologram.entityIds;
        if (entities != null) {
            hologram.player.sendPacket(new DestroyEntitiesPacket(entities));
        }
    }

    public static void removePlayer(CytosisPlayer player) {
        holograms.removeIf(hologram -> {
            if (hologram.getPlayer().equals(player)) {
                player.sendPacket(new DestroyEntitiesPacket(hologram.entityIds));
                return true;
            }
            return false;
        });
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
        @Builder.Default
        private final List<Integer> entityIds = new ArrayList<>();
    }
}
