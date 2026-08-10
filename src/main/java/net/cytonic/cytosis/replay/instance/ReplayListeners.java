package net.cytonic.cytosis.replay.instance;

import java.nio.file.Path;
import java.util.Map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Metadata.Entry;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.instance.InstanceBlockUpdateEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.inventory.CreativeInventoryActionEvent;
import net.minestom.server.event.inventory.InventoryClickEvent;
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
import net.minestom.server.item.ItemStack;
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

    private static void handleInventory(InstanceRecorder r, ItemStack item, int rawSlot, CytosisPlayer player) {
        EquipmentSlot slot = translateSlot(rawSlot);
        if (slot == null) return;
        if (slot == EquipmentSlot.MAIN_HAND) {
            if (rawSlot != player.getHeldSlot()) return;
        }
        r.updateEquipment(player, slot, item);
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
            i.eventNode()
                .addListener(InstanceTickEvent.class, _ -> r.tick())
                .addListener(PlayerSpawnEvent.class, event -> {
                    if (!(event.getEntity() instanceof CytosisPlayer player)) return;
                    r.onPlayerJoin(player);
                })
                .addListener(RemoveEntityFromInstanceEvent.class, event -> {
                    if (!(event.getEntity() instanceof CytosisPlayer player)) return;
                    r.onPlayerLeave(player);
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
                .addListener(InventoryClickEvent.class, event -> {
                    if (!(event.getInventory() instanceof PlayerInventory)) return;
                    if (!(event.getPlayer() instanceof CytosisPlayer player)) return;
                    handleInventory(r, event.getClickedItem(), event.getSlot(), player);
                })
                .addListener(CreativeInventoryActionEvent.class, event -> {
                    if (!(event.getPlayer() instanceof CytosisPlayer player)) return;
                    handleInventory(r, event.getClickedItem(), event.getSlot(), player);
                })
                .addListener(PlayerSendMessageEvent.class,
                    e -> r.recordChat(e.getPlayer().getEntityId(), e.getMessage(), e.getChannel()))
                .addListener(PlayerCommandEvent.class, event -> {
                    if (event.isCancelled()) return;
                    r.recordCommand(event.getPlayer().getEntityId(), event.getCommand());
                });

            MinecraftServer.getGlobalEventHandler()
                .addListener(InventoryItemChangeEvent.class, event -> {
                    if (!(event.getInventory() instanceof PlayerInventory inv)) return;
                    EquipmentSlot slot = translateSlot(event.getSlot());
                    if (slot == null) return;
                    r.updateSlotByInventory(inv, slot, event.getNewItem(), event.getSlot());
                })
                .addListener(PlayerPacketOutEvent.class, e -> {
                    if (!(e.getPlayer() instanceof CytosisPlayer player)) return;
                    if (player.getInstance() == null) return;
                    if (!player.getInstance().getUuid().equals(i.getUuid())) return;
                    if (e.getPacket() instanceof EntityMetaDataPacket(int id, Map<Integer, Entry<?>> map)) {
                        r.updateMeta(id, map);
                    }
                });
        });
    }
}
