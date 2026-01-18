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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisBootstrap;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.entity.hologram.PlayerHolograms;
import net.cytonic.cytosis.entity.newnpx.NPC;
import net.cytonic.cytosis.entity.newnpx.configuration.NPCConfiguration;
import net.cytonic.cytosis.entity.newnpx.impl.NPCEntityImpl;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;

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
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    NPC npc = (NPC) constructor.newInstance();
                    register(npc);
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

    public void updateForPlayer(CytosisPlayer player) {
        registeredNPCs.forEach(npc -> {
            NPCConfiguration config = npc.getConfig();
            if (!playerNPCs.containsKey(player.getUuid())) {
                //has not been loaded by player before
                List<Component> holograms = config.holograms(player);
                String username = toString(holograms.getLast());
                boolean overflowing = username.length() > 16;
                if (overflowing) {
                    username = " ";
                }

                NPCEntityImpl entity = new NPCEntityImpl(
                    username,
                    config.texture(player),
                    config.signature(player),
                    holograms
                );
                entity.setInstance(config.instance(), config.position(player));
                entity.addViewer(player);

                PlayerHolograms.Hologram hologram = PlayerHolograms.Hologram.builder()
                    .pos(config.position(player).add(0, 1.1 + (overflowing ? -0.2f : 0.0f), 0))
                    .lines(holograms.subList(0, holograms.size() - (overflowing ? 0 : 1)))
                    .player(player)
                    .instance(config.instance())
                    .build();

                PlayerHolograms.addHologram(hologram);
                playerNPCs.computeIfAbsent(player.getUuid(), _ -> new HashMap<>())
                    .put(npc, Tuple.of(entity, hologram));
                return;
            }
            //player has seen it before
            Map<NPC, Tuple<NPCEntityImpl, PlayerHolograms.Hologram>> playerNpcs = playerNPCs.get(player.getUuid());
            NPCEntityImpl entity = playerNpcs.get(npc).getFirst();

            if (player.getDistance(config.position(player)) <= NPC.SPAWN_DISTANCE) {
            }
        });
    }

    private String toString(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}