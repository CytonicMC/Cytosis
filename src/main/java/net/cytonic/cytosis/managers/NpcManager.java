package net.cytonic.cytosis.managers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisBootstrap;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.entity.npc.NPC;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;
import net.cytonic.protocol.utils.ExcludeFromClassGraph;

/**
 * A class that manages NPCs
 */
@CytosisComponent(dependsOn = PluginManager.class)
public class NpcManager implements Bootstrappable {

    @Getter
    private static final Map<UUID, NPC> registeredNPCs = new ConcurrentHashMap<>();

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
        registeredNPCs.put(npc.getUuid(), npc);
    }

    public void removePlayer(CytosisPlayer player) {
        registeredNPCs.values().forEach(npc -> npc.removePlayer(player));
    }

    public void removeNPC(NPC npc) {
        registeredNPCs.remove(npc.getUuid());
    }

    public void updateForPlayer(CytosisPlayer player) {
        registeredNPCs.values().forEach(npc -> npc.updateForPlayer(player));
    }

    public NPC getNPC(CytosisPlayer player, int entityId) {
        for (NPC npc : registeredNPCs.values()) {
            if (npc.hasEntityId(player, entityId)) {
                return npc;
            }
        }
        return null;
    }
}