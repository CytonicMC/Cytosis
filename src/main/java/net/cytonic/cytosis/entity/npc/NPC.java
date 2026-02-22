package net.cytonic.cytosis.entity.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.ExpiringMap;
import net.cytonic.cytosis.entity.hologram.PlayerHolograms;
import net.cytonic.cytosis.entity.npc.configuration.NPCConfiguration;
import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.impl.NPCEntityImpl;
import net.cytonic.cytosis.entity.npc.pathfinding.NavigatePathGoal;
import net.cytonic.cytosis.entity.npc.pathfinding.Path;
import net.cytonic.cytosis.events.npcs.NPCInteractEvent;
import net.cytonic.cytosis.managers.NpcManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.MetadataPacketBuilder;

public abstract class NPC {

    public static final int SPAWN_DISTANCE = 48;
    public static final int SPAWN_DISTANCE_SQUARED = SPAWN_DISTANCE * SPAWN_DISTANCE;
    public static final int LOOK_DISTANCE = 5;
    public static final int LOOK_DISTANCE_SQUARED = LOOK_DISTANCE * LOOK_DISTANCE;

    @Getter
    private final UUID uuid = UUID.randomUUID();
    @Getter
    private final NPCConfiguration config;
    @Getter
    private final Component name;

    private final Map<UUID, ViewerData> viewers = new ExpiringMap<>();
    private final Map<UUID, Dialog<? extends CytosisPlayer>> activeDialogs = new ExpiringMap<>();

    public NPC(NPCConfiguration config) {
        this.config = config;
        String className = getClass().getSimpleName().replaceAll("NPC", "");
        this.name =
            config.chatName() != null ? config.chatName()
                : Component.text(className.replaceAll("(?<=.)(?=\\p{Lu})", " "));
    }

    @OverrideOnly
    public void onClick(NPCInteractEvent event) {
    }

    public boolean hasActiveDialog(CytosisPlayer player) {
        return activeDialogs.containsKey(player.getUuid());
    }

    protected <P extends CytosisPlayer> Dialog<P> startDialog(P player) {
        if (activeDialogs.containsKey(player.getUuid())) {
            //noinspection unchecked
            return (Dialog<P>) activeDialogs.get(player.getUuid());
        }
        Dialog<P> dialog = new Dialog<>(this);
        activeDialogs.put(player.getUuid(), dialog);
        return dialog;
    }

    public void register() {
        Cytosis.get(NpcManager.class).register(this);
    }

    public void remove() {
        viewers.forEach((uuid, data) ->
            Cytosis.getPlayer(uuid).ifPresent(player -> {
                player.sendPacket(new DestroyEntitiesPacket(List.of(data.entity().getEntityId())));
                PlayerHolograms.removeHologram(data.hologram());
            }));
        viewers.clear();
        Cytosis.get(NpcManager.class).removeNPC(this);
    }

    public void updateForPlayer(CytosisPlayer player) {
        Pos npcPos = config.position(player);
        double distanceSquared = player.getPosition().distanceSquared(npcPos);
        boolean inRange = distanceSquared <= SPAWN_DISTANCE_SQUARED;

        ViewerData data = viewers.get(player.getUuid());

        if (data == null) {
            if (!inRange) return;

            // has not been loaded by player before
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

            sendPackets(player, entity);

            MinecraftServer.getSchedulerManager()
                .scheduleTask(() -> player.sendPacket(new PlayerInfoRemovePacket(entity.getUuid())),
                    TaskSchedule.tick(2), TaskSchedule.stop());

            PlayerHolograms.Hologram hologram = PlayerHolograms.Hologram.builder()
                .pos(config.position(player).add(0, 2.0, 0))
                .lines(holograms)
                .player(player)
                .instance(config.instance())
                .build();

            PlayerHolograms.addHologram(hologram);
            viewers.put(player.getUuid(), new ViewerData(entity, hologram));
            return;
        }

        // player has seen it before
        NPCEntityImpl entity = data.entity();
        PlayerHolograms.Hologram hologram = data.hologram();

        if (!inRange) {
            player.sendPacket(new DestroyEntitiesPacket(List.of(entity.getEntityId())));
            PlayerHolograms.removeHologram(hologram);
            viewers.remove(player.getUuid());
            return;
        }

        player.sendPacket(new EntityTeleportPacket(entity.getEntityId(), entity.getPosition(), Vec.ZERO, 0, false));
        PlayerHolograms.updateHologram(hologram, entity.getPosition().add(0, 2.0, 0));

        if (config.looking(player) && distanceSquared <= LOOK_DISTANCE_SQUARED) {
            Pos lookPos = entity.getPosition().withLookAt(player.getPosition().add(0, player.getEyeHeight(), 0));
            player.sendPackets(
                new EntityRotationPacket(entity.getEntityId(), lookPos.yaw(), lookPos.pitch(), player.isOnGround()),
                new EntityHeadLookPacket(entity.getEntityId(), lookPos.yaw()));
        } else {
            player.sendPackets(
                new EntityRotationPacket(entity.getEntityId(), entity.getPosition().yaw(),
                    entity.getPosition().pitch(), player.isOnGround()),
                new EntityHeadLookPacket(entity.getEntityId(), entity.getPosition().yaw()));
        }
    }

    public void removePlayer(CytosisPlayer player) {
        activeDialogs.remove(player.getUuid());
        ViewerData data = viewers.remove(player.getUuid());
        if (data != null) {
            player.sendPacket(new DestroyEntitiesPacket(List.of(data.entity().getEntityId())));
            PlayerHolograms.removeHologram(data.hologram());
        }
    }

    public void removeDialog(CytosisPlayer player) {
        activeDialogs.remove(player.getUuid());
    }

    public boolean hasEntityId(CytosisPlayer player, int entityId) {
        ViewerData data = viewers.get(player.getUuid());
        return data != null && data.entity().getEntityId() == entityId;
    }

    private void sendPackets(CytosisPlayer player, NPCEntityImpl entity) {
        List<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
        if (entity.getSkinTexture() != null && entity.getSkinSignature() != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", entity.getSkinTexture(),
                entity.getSkinSignature()));
        }
        Pos npcPos = entity.getPosition();
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

    private record ViewerData(NPCEntityImpl entity, PlayerHolograms.Hologram hologram) {

    }
}