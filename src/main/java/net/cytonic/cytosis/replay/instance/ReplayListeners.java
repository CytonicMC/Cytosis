package net.cytonic.cytosis.replay.instance;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Metadata.Entry;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.instance.InstanceBlockUpdateEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket.Animation;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.events.network.PlayerSendMessageEvent;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.replay.io.SegmentWriter;
import net.cytonic.cytosis.utils.Events;

@CytosisComponent
public class ReplayListeners implements Bootstrappable {

    private static final Map<UUID, InstanceRecorder> RECORDERS = new ConcurrentHashMap<>();
    private static final AtomicBoolean GLOBAL_LISTENERS_REGISTERED = new AtomicBoolean();

    @Nullable
    private static EquipmentSlot translateSlot(int slot) {
        if (slot <= 8) return EquipmentSlot.MAIN_HAND;
        return switch (slot) {
            case 41 -> EquipmentSlot.HELMET;
            case 42 -> EquipmentSlot.CHESTPLATE;
            case 43 -> EquipmentSlot.LEGGINGS;
            case 44 -> EquipmentSlot.BOOTS;
            case 45 -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void init() {
        Events.onInstanceRegister(ev -> {
            Instance i = ev.getInstance();
            if (i.getTag(InstanceRecorder.RECORDING_EXEMPT)) return;
            if (i.hasTag(InstanceRecorder.RECORDER)) return;
            InstanceRecorder r = new InstanceRecorder(i, new SegmentWriter(Path.of("recordings/raw/" + i.getUuid())));
            i.setTag(InstanceRecorder.RECORDER, r);
            RECORDERS.put(i.getUuid(), r);
            i.eventNode()
                .addListener(InstanceTickEvent.class, _ -> r.tick())
                .addListener(PlayerSpawnEvent.class, event -> {
                    if (!(event.getEntity() instanceof CytosisPlayer player)) return;
                    r.onPlayerJoin(player);
                })
                .addListener(RemoveEntityFromInstanceEvent.class, event -> {
                    if (event.getEntity() instanceof CytosisPlayer player) {
                        r.onPlayerLeave(player);
                    } else {
                        r.onEntityDespawn(event.getEntity());
                    }
                })
                .addListener(EntitySpawnEvent.class, event -> {
                    if (event.getEntity() instanceof Player) return;
                    r.onEntitySpawn(event.getEntity());
                })
                .addListener(PlayerHandAnimationEvent.class, event -> {
                    if (!(event.getEntity() instanceof CytosisPlayer player)) return;
                    r.animate(player,
                        event.getHand() == PlayerHand.MAIN ? Animation.SWING_MAIN_ARM : Animation.SWING_OFF_HAND);
                })
                .addListener(InstanceBlockUpdateEvent.class,
                    event -> r.updateBlock(event.getBlockPosition(), event.getBlock(), null))
                .addListener(PlayerBlockBreakEvent.class,
                    e -> r.updateBlock(e.getBlockPosition(), e.getResultBlock(), e.getBlock()))
                .addListener(PlayerBlockPlaceEvent.class, e -> r.updateBlock(e.getBlockPosition(), e.getBlock(),
                    e.getInstance().getBlock(e.getBlockPosition())))
                .addListener(PlayerChangeHeldSlotEvent.class, event -> {
                    if (!(event.getPlayer() instanceof CytosisPlayer player)) return;
                    r.updateEquipment(player, EquipmentSlot.MAIN_HAND, event.getItemInNewSlot());
                })
                .addListener(PlayerSendMessageEvent.class,
                    e -> r.recordChat(e.getPlayer().getEntityId(), e.getMessage(), e.getChannel()))
                .addListener(PlayerCommandEvent.class, event -> {
                    if (event.isCancelled()) return;
                    r.recordCommand(event.getPlayer().getEntityId(), event.getCommand());
                });
        });

        Events.onInstanceUnregister(ev -> {
            InstanceRecorder recorder = RECORDERS.remove(ev.getInstance().getUuid());
            if (recorder != null) recorder.close();
        });

        registerGlobalListeners();
    }

    private void registerGlobalListeners() {
        // These listeners only need to exist once for the whole server. Registering them per instance leaked one
        // listener (and its recorder/instance references) for every instance ever created.
        if (!GLOBAL_LISTENERS_REGISTERED.compareAndSet(false, true)) return;
        MinecraftServer.getGlobalEventHandler()
            .addListener(InventoryItemChangeEvent.class, event -> {
                if (!(event.getInventory() instanceof PlayerInventory inv)) return;
                EquipmentSlot slot = translateSlot(event.getSlot());
                if (slot == null) return;
                for (InstanceRecorder recorder : RECORDERS.values()) {
                    recorder.updateSlotByInventory(inv, slot, event.getNewItem(), event.getSlot());
                }
            })
            .addListener(PlayerPacketOutEvent.class, e -> {
                if (!(e.getPlayer() instanceof CytosisPlayer player)) return;
                if (player.getInstance() == null) return;
                InstanceRecorder recorder = RECORDERS.get(player.getInstance().getUuid());
                if (recorder == null) return;
                if (e.getPacket() instanceof EntityMetaDataPacket(int id, Map<Integer, Entry<?>> map)) {
                    recorder.updateMeta(id, map);
                }
            });
    }
}
