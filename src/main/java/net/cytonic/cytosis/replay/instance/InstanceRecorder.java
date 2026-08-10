package net.cytonic.cytosis.replay.instance;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket.Animation;
import net.minestom.server.tag.Tag;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.objects.BiMap;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.replay.ReplayBundle;
import net.cytonic.cytosis.replay.ReplayEvent;
import net.cytonic.cytosis.replay.ReplayEvent.BlockUpdate;
import net.cytonic.cytosis.replay.ReplayEvent.ChatMessage;
import net.cytonic.cytosis.replay.ReplayEvent.Command;
import net.cytonic.cytosis.replay.ReplayEvent.EquipmentUpdate;
import net.cytonic.cytosis.replay.ReplayEvent.MetaUpdate;
import net.cytonic.cytosis.replay.ReplayEvent.PlayerIdent;
import net.cytonic.cytosis.replay.ReplayEvent.PlayerJoin;
import net.cytonic.cytosis.replay.ReplayEvent.PlayerLeave;
import net.cytonic.cytosis.replay.ReplayEvent.PosRot;
import net.cytonic.cytosis.replay.io.MemoryBuffer;
import net.cytonic.cytosis.replay.io.SegmentWriter;
import net.cytonic.cytosis.utils.Utils;
import net.cytonic.cytosis.world.AbstractWorld;

@Slf4j
public class InstanceRecorder {

    public static final Tag<InstanceRecorder> RECORDER = Tag.Transient("cytosis:instance_recorder");
    public static final Tag<Boolean> RECORDING_EXEMPT = Tag.Boolean("cytosis:recording_exempt").defaultValue(false);

    static {
        // remove old recordings on start
        try {
            FileUtils.deleteDirectory(Path.of("recordings/raw").toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Instance instance;
    private final Instant recorderStart = Instant.now();
    private final SegmentWriter writer;
    private final BiMap<UUID, Integer> trackedEntityIds = new BiMap<>();
    private final BiMap<UUID, PlayerInventory> trackedInventories = new BiMap<>();

    private final MemoryBuffer memoryBuffer = new MemoryBuffer();
    private final AtomicInteger currentTick = new AtomicInteger(0);
    private final Map<Integer, Pos> previousEntityPositions = new HashMap<>();
    private boolean needsAbsPosRot = true;
    private boolean needsIdentEvent = true;


    public InstanceRecorder(Instance instance, SegmentWriter writer) {
        this.instance = instance;
        this.writer = writer;
        writer.setOnRotate(_ -> {
            needsAbsPosRot = true;
            needsIdentEvent = true;
        });
    }

    /**
     * Called from your instance's join listener.
     */
    public void onPlayerJoin(CytosisPlayer player) {
        int entityId = player.getEntityId();
        trackedEntityIds.put(player.getUuid(), entityId);
        trackedInventories.put(player.getUuid(), player.getInventory());
        memoryBuffer.addEvent(new PlayerIdent(currentTick.get(), Map.of(entityId, player.getUuid())));
        memoryBuffer.addEvent(new PlayerJoin(currentTick.get(), entityId));
        memoryBuffer.addEvent(
            new EquipmentUpdate(currentTick.get(), entityId, player.getEquipmentsPacket().equipments()));
    }

    /**
     * Called from your instance's disconnect/instance-change listener.
     */
    public void onPlayerLeave(CytosisPlayer player) {
        int entityId = player.getEntityId();
        trackedEntityIds.removeByValue(entityId);
        trackedInventories.removeByKey(player.getUuid());
        memoryBuffer.addEvent(new PlayerLeave(currentTick.get(), entityId));
    }

    public void animate(CytosisPlayer player, Animation animation) {
        memoryBuffer.addEvent(new ReplayEvent.Animation(currentTick.get(), player.getEntityId(), animation));
    }

    public void updateBlock(Point pos, Block newBlock, @Nullable Block old) {
        memoryBuffer.addEvent(new BlockUpdate(currentTick.get(), pos, newBlock, old));
    }

    public void updateEquipment(CytosisPlayer player, EquipmentSlot slot, ItemStack item) {
        memoryBuffer.addEvent(new EquipmentUpdate(currentTick.get(), player.getEntityId(), Utils.map(slot, item)));
    }

    public void updateSlotByInventory(PlayerInventory inventory, EquipmentSlot slot, ItemStack item, int rawSlot) {
        UUID player = trackedInventories.getByValue(inventory);
        if (player == null) return;
        if (slot == EquipmentSlot.MAIN_HAND) {
            CytosisPlayer p = Cytosis.getPlayer(player).orElse(null);
            if (p == null) return;
            if (rawSlot != p.getHeldSlot()) return;
        }
        Integer id = trackedEntityIds.getByKey(player);
        if (id == null) return;
        memoryBuffer.addEvent(new EquipmentUpdate(currentTick.get(), id, Utils.map(slot, item)));
    }

    public void updateMeta(int entityId, Map<Integer, Metadata.Entry<?>> entries) {
        memoryBuffer.addEvent(new MetaUpdate(currentTick.get(), entityId, entries));
    }

    public void recordChat(int entityId, Component message, ChatChannel channel) {
        memoryBuffer.addEvent(new ChatMessage(currentTick.get(), entityId, message, channel));
    }

    public void recordCommand(int entityId, String command) {
        memoryBuffer.addEvent(new Command(currentTick.get(), entityId, command));
    }

    /**
     * Called once per server tick for this instance.
     */
    public void tick() {
        currentTick.incrementAndGet();
        PosRot pr = takePosRotSnapshot();
        if (pr != null && !pr.entries().isEmpty()) {
            // don't record it if its empty
            memoryBuffer.addEvent(pr);
        }

        if (needsIdentEvent) {
            memoryBuffer.addEvent(takeIdentSnapshot());
        }

        if (memoryBuffer.shouldFlush()) {
            byte[] chunk = memoryBuffer.drainAndSerialize();
            writer.appendAsync(chunk);
        }
    }

    @Blocking
    public void flushBuffers() {
        byte[] chunk = memoryBuffer.drainAndSerialize();
        writer.appendSync(chunk);
    }

    private PlayerIdent takeIdentSnapshot() {
        Map<Integer, UUID> idents = new HashMap<>();
        for (Player player : instance.getPlayers()) {
            idents.put(player.getEntityId(), player.getUuid());
        }
        needsIdentEvent = false;
        return new PlayerIdent(currentTick.get(), idents);
    }

    private PosRot takePosRotSnapshot() {
        Map<Integer, Pos> values = new HashMap<>();
        Set<Integer> absolute = new HashSet<>();
        for (Entity e : instance.getEntities()) {
            Pos current = e.getPosition();
            Pos prev = previousEntityPositions.get(e.getEntityId());
            previousEntityPositions.put(e.getEntityId(), current);
            if (needsAbsPosRot || prev == null) {
                values.put(e.getEntityId(), current); // absolute, not delta
                absolute.add(e.getEntityId());
                continue;
            }

            Pos delta = current.sub(prev);
            if (delta.lengthSquared() >= 63.9960938096) {
                // max length of a standard move packet, so must be absolute
                values.put(e.getEntityId(), current);
                absolute.add(e.getEntityId());
                continue;
            }

            if (delta.samePoint(Pos.ZERO, 0.001) && current.sameView(prev)) continue;
            values.put(e.getEntityId(), delta);
        }
        if (values.isEmpty()) return null;
        needsAbsPosRot = false;
        return new PosRot(values, absolute, currentTick.get());
    }


    public int tickAt(Instant instant) {
        long millisSinceStart = Duration.between(recorderStart, instant).toMillis();
        return (int) (millisSinceStart / 50L);
    }

    public Instant instantAt(int tick) {
        return recorderStart.plusMillis(tick * 50L);
    }

    public ReplayBundle flush(Instant triggerTime, Duration window) {
        int fromTick = tickAt(triggerTime.minus(window));
        int toTick = tickAt(triggerTime);
        return writer.exportRange(triggerTime.minus(window), triggerTime, fromTick, toTick, getMap());
    }

    private Key getMap() {
        return instance.getTag(AbstractWorld.MAP_ID);
    }
}
