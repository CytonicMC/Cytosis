package net.cytonic.cytosis.entity.newnpx.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.utils.MetadataPacketBuilder;

@Getter
public class NPCEntityImpl extends Entity {

    private final ArrayList<Player> packetsSent = new ArrayList<>();
    private final String username;

    private final String skinTexture;
    private final String skinSignature;
    private final List<Component> holograms;

    public NPCEntityImpl(String username,
        @Nullable String skinTexture, @Nullable String skinSignature, @NotNull List<Component> holograms) {
        super(EntityType.PLAYER, UUID.randomUUID());
        this.username = username;

        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
        this.holograms = holograms;

        setNoGravity(true);
        setAutoViewable(false);
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);

        List<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
        if (skinTexture != null && skinSignature != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", skinTexture, skinSignature));
        }

        player.sendPackets(
            new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                new PlayerInfoUpdatePacket.Entry(
                    getUuid(),
                    username,
                    properties,
                    false,
                    0,
                    GameMode.ADVENTURE,
                    Component.text("HELLO WORLD"),
                    null,
                    0,
                    false)),
            new SpawnEntityPacket(this.getEntityId(), this.getUuid(), EntityType.PLAYER,
                getPosition(),
                (float) 0,
                0,
                Vec.ZERO),
            new EntityHeadLookPacket(getEntityId(), getPosition().yaw()),
            MetadataPacketBuilder.empty(getEntityId()).setByte(
                MetadataDef.Avatar.DISPLAYED_MODEL_PARTS_FLAGS.index(),
                (byte) 127
            ).build()
        );

        packetsSent.add(player);
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (packetsSent.contains(player)) {
                player.sendPacket(new PlayerInfoRemovePacket(getUuid()));
            }
        }, TaskSchedule.tick(2), TaskSchedule.stop());
    }

    @Override
    public void updateOldViewer(@NotNull Player player) {
        super.updateOldViewer(player);
        player.sendPacket(new PlayerInfoRemovePacket(getUuid()));
        packetsSent.remove(player);
    }

    @Override
    public void tick(long time) {
        Instance instance = getInstance();
        if (instance == null) {
            return;
        }

        Pos position = getPosition();

        if (!instance.isChunkLoaded(position)) {
            instance.loadChunk(position).join();
        }

        super.tick(time);
    }
}