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
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityRotationPacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
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
import net.cytonic.cytosis.entity.npc.pathfinding.NavigatePathGoal;
import net.cytonic.cytosis.entity.npc.pathfinding.Path;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;
import net.cytonic.cytosis.utils.MetadataPacketBuilder;
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
        playerNPCs.forEach((uuid, map) ->
            Cytosis.getPlayer(uuid).ifPresent(player ->
                map.forEach((mapNpc, tuple) -> {
                    if (npc.equals(mapNpc)) {
                        player.sendPacket(new DestroyEntitiesPacket(List.of(tuple.getFirst().getEntityId())));
                        PlayerHolograms.removeHologram(tuple.getSecond());
                    }
                })));
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
                String username = "";

                NPCEntityImpl entity = new NPCEntityImpl(
                    username,
                    config.texture(player),
                    config.signature(player),
                    holograms
                );
                entity.setInstance(config.instance(), npcPos);
                Path path = config.path(player);
                if (path != null) {
                    EntityAIGroup group = new EntityAIGroup();
                    group.getGoalSelectors().add(new NavigatePathGoal(entity, path));
                    entity.addAIGroup(group);
                }

                sendPackets(player, entity, config);

                MinecraftServer.getSchedulerManager()
                    .scheduleTask(() -> player.sendPacket(new PlayerInfoRemovePacket(entity.getUuid())),
                        TaskSchedule.tick(2), TaskSchedule.stop());

                PlayerHolograms.Hologram hologram = PlayerHolograms.Hologram.builder()
                    .pos(config.position(player).add(0, 1.8, 0))
                    .lines(holograms)
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
            Tuple<NPCEntityImpl, PlayerHolograms.Hologram> data = playerNpcs.get(npc);
            NPCEntityImpl entity = data.getFirst();
            PlayerHolograms.Hologram hologram = data.getSecond();

            if (!inRange) {
                player.sendPacket(new DestroyEntitiesPacket(List.of(entity.getEntityId())));
                PlayerHolograms.removeHologram(hologram);
                playerNpcs.remove(npc);
                return;
            }

            if (config.looking(player) && distanceSquared <= (NPC.LOOK_DISTANCE * NPC.LOOK_DISTANCE)) {
                Pos lookPos = entity.getPosition().withLookAt(player.getPosition().add(0, player.getEyeHeight(), 0));
                player.sendPackets(
                    new EntityRotationPacket(entity.getEntityId(), lookPos.yaw(), lookPos.pitch(), player.isOnGround()),
                    new EntityHeadLookPacket(entity.getEntityId(), lookPos.yaw()));
            } else {
                player.sendPackets(
                    new EntityRotationPacket(entity.getEntityId(), entity.getPosition().yaw(),
                        entity.getPosition().pitch(),
                        player.isOnGround()),
                    new EntityHeadLookPacket(entity.getEntityId(), entity.getPosition().yaw()));
            }
            player.sendPacket(new EntityTeleportPacket(entity.getEntityId(), entity.getPosition(), Vec.ZERO, 0, false));
            PlayerHolograms.updateHologram(hologram, entity.getPosition().add(0, 1.8, 0));
        });
    }

    public NPC getNPC(CytosisPlayer player, int entityId) {
        if (!playerNPCs.containsKey(player.getUuid())) return null;
        for (Map.Entry<NPC, Tuple<NPCEntityImpl, PlayerHolograms.Hologram>> entry : playerNPCs.get(player.getUuid())
            .entrySet()) {
            if (entry.getValue().getFirst().getEntityId() == entityId) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void sendPackets(CytosisPlayer player, NPCEntityImpl entity, NPCConfiguration config) {
        List<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
        if (entity.getSkinTexture() != null && entity.getSkinSignature() != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", entity.getSkinTexture(),
                entity.getSkinSignature()));
        }
        Pos npcPos = config.position(player);
        player.sendPackets(
            new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                new PlayerInfoUpdatePacket.Entry(
                    entity.getUuid(),
                    entity.getUsername(),
                    properties,
                    false,
                    0,
                    GameMode.ADVENTURE,
                    Component.empty(),
                    null,
                    0,
                    false)),
            new SpawnEntityPacket(entity.getEntityId(), entity.getUuid(), EntityType.PLAYER,
                npcPos,
                npcPos.yaw(),
                0,
                Vec.ZERO),
            new EntityHeadLookPacket(entity.getEntityId(), npcPos.yaw()),
            MetadataPacketBuilder.empty(entity.getEntityId())
                .setByte(MetadataDef.Avatar.DISPLAYED_MODEL_PARTS_FLAGS.index(), (byte) 127)
                .setBoolean(MetadataDef.Player.HAS_NO_GRAVITY.index(), true)
                .build(),
            new TeamsPacket("npc_team", new TeamsPacket.CreateTeamAction(
                Component.text("NPCs"),
                (byte) 0,
                TeamsPacket.NameTagVisibility.NEVER,
                TeamsPacket.CollisionRule.NEVER,
                NamedTextColor.WHITE,
                Component.empty(),
                Component.empty(),
                List.of(entity.getUsername())
            ))
        );
    }
}