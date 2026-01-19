package net.cytonic.cytosis.managers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.timer.TaskSchedule;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisBootstrap;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.entity.hologram.PlayerHolograms;
import net.cytonic.cytosis.entity.npc.NPC;
import net.cytonic.cytosis.entity.npc.configuration.NPCConfiguration;
import net.cytonic.cytosis.entity.npc.impl.NPCEntityImpl;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;
import net.cytonic.protocol.ExcludeFromClassGraph;

/**
 * A class that manages NPCs
 */
@CytosisComponent(dependsOn = PluginManager.class)
public class NpcManager implements Bootstrappable {

    @Getter
    private static final List<NPC> registeredNPCs = new ArrayList<>();
    @Getter
    private static final Map<UUID, Map<NPC, Tuple<NPCEntityImpl, PlayerHolograms.Hologram>>> playerNPCs = new HashMap<>();

    @Override
    public void init() {
        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Cytosis.class.getClassLoader());
        loaders.addAll(PluginClassLoader.LOADERS);

        ClassGraph graph = new ClassGraph()
            .acceptPackages(CytosisBootstrap.SCAN_PACKAGE_ROOT)
            .enableAllInfo()
            .overrideClassLoaders(loaders.toArray(new ClassLoader[0]));
        try (ScanResult result = graph.scan()) {
            result.getSubclasses(NPC.class).loadClasses().forEach(clazz -> {
                try {
                    if (clazz.isAnnotationPresent(ExcludeFromClassGraph.class)) return;
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    NPC npc = (NPC) constructor.newInstance();
                    npc.register();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                updateForPlayer(player);
            }
        }, TaskSchedule.tick(2), TaskSchedule.tick(2));
    }

    public void register(NPC npc) {
        registeredNPCs.add(npc);
    }

    public void removePlayer(CytosisPlayer player) {
        Map<NPC, Tuple<NPCEntityImpl, PlayerHolograms.Hologram>> playerNpcs = playerNPCs.remove(player.getUuid());
        if (playerNpcs != null) {
            playerNpcs.keySet().forEach(this::removeNPC);
        }
    }

    public void removeNPC(NPC npc) {
        playerNPCs.forEach((_, map) -> map.forEach((mapNpc, tuple) -> {
            if (npc.equals(mapNpc)) {
                tuple.getFirst().remove();
                if (PlayerHolograms.holograms.containsKey(tuple.getSecond())) {
                    PlayerHolograms.holograms.remove(tuple.getSecond()).forEach(Entity::remove);
                }
            }
        }));
    }

    public void updateForPlayer(CytosisPlayer player) {
        registeredNPCs.forEach(npc -> {
            NPCConfiguration config = npc.getConfig();
            Pos npcPos = config.position(player);
            double distanceSquared = player.getPosition().distanceSquared(npcPos);
            boolean inRange = distanceSquared <= (NPC.SPAWN_DISTANCE * NPC.SPAWN_DISTANCE);

            if (!playerNPCs.containsKey(player.getUuid()) || !playerNPCs.get(player.getUuid()).containsKey(npc)) {
                if (!inRange) return;

                //has not been loaded by player before
                List<Component> holograms = config.holograms(player);
                String username = " ";

                NPCEntityImpl entity = new NPCEntityImpl(
                    username,
                    config.texture(player),
                    config.signature(player),
                    holograms
                );
                entity.setInstance(config.instance(), npcPos);
                entity.addViewer(player);

                PlayerHolograms.Hologram hologram = PlayerHolograms.Hologram.builder()
                    .pos(config.position(player).add(0, 1.1, 0))
                    .lines(holograms)
                    .player(player)
                    .instance(config.instance())
                    .build();

                PlayerHolograms.addHologram(hologram, entity);
                playerNPCs.computeIfAbsent(player.getUuid(), _ -> new HashMap<>())
                    .put(npc, Tuple.of(entity, hologram));
                return;
            }

            //player has seen it before
            Map<NPC, Tuple<NPCEntityImpl, PlayerHolograms.Hologram>> playerNpcs = playerNPCs.get(player.getUuid());
            Tuple<NPCEntityImpl, PlayerHolograms.Hologram> data = playerNpcs.get(npc);
            NPCEntityImpl entity = data.getFirst();
            PlayerHolograms.Hologram hologram = data.getSecond();

            if (!inRange) {
                entity.remove();
                if (PlayerHolograms.holograms.containsKey(hologram)) {
                    PlayerHolograms.holograms.remove(hologram).forEach(Entity::remove);
                }
                playerNpcs.remove(npc);
                return;
            }

            if (config.looking(player) && distanceSquared <= (NPC.LOOK_DISTANCE * NPC.LOOK_DISTANCE)) {
                Pos lookPos = entity.getPosition().withLookAt(player.getPosition().add(0, player.getEyeHeight(), 0));
                entity.setView(lookPos.yaw(), lookPos.pitch());
            } else {
                entity.setView(npcPos.yaw(), npcPos.pitch());
            }
        });
    }

    public NPC getNPC(CytosisPlayer player, Entity entity) {
        if (!playerNPCs.containsKey(player.getUuid())) return null;
        for (Map.Entry<NPC, Tuple<NPCEntityImpl, PlayerHolograms.Hologram>> entry : playerNPCs.get(player.getUuid())
            .entrySet()) {
            if (entry.getValue().getFirst().getEntityId() == entity.getEntityId()) {
                return entry.getKey();
            }
        }
        return null;
    }
}