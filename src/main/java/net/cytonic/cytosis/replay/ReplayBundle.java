package net.cytonic.cytosis.replay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.replay.ReplayEvent.BlockUpdate;
import net.cytonic.cytosis.replay.ReplayEvent.EquipmentUpdate;
import net.cytonic.cytosis.replay.ReplayEvent.PlayerIdent;
import net.cytonic.cytosis.replay.ReplayEvent.PlayerJoin;

/**
 * An object representing a complete replay, having potentially been aggregated over several segments.
 *
 * @param idents
 * @param events
 * @param meta
 */
public record ReplayBundle(UUID uuid, Map<Integer, UUID> idents, List<ReplayEvent> events, ReplayMeta meta) {

    public static NetworkBuffer.Type<ReplayBundle> NETWORK_TYPE = NetworkBufferTemplate.template(
        NetworkBuffer.UUID, ReplayBundle::uuid,
        NetworkBuffer.VAR_INT.mapValue(NetworkBuffer.UUID), ReplayBundle::idents,
        ReplayEvent.NETWORK_TYPE.list(), ReplayBundle::events,
        ReplayMeta.NETWORK_TYPE, ReplayBundle::meta,
        ReplayBundle::new
    );

    /**
     * Creates a new replay bundle
     */
    public ReplayBundle(List<ReplayEvent> events, ReplayMeta meta) {
        events.sort(Comparator.comparingInt(ReplayEvent::tick));
        Map<Integer, UUID> idents = normalize(events);
        this(UUID.randomUUID(), idents, events, meta);
    }

    public static Map<Integer, UUID> normalize(List<ReplayEvent> events) {
        int eventsStart = events.size();
        final Map<EquipmentSlot, ItemStack> defaultEquipment = new HashMap<>(Map.of(
            EquipmentSlot.MAIN_HAND, ItemStack.AIR,
            EquipmentSlot.OFF_HAND, ItemStack.AIR,
            EquipmentSlot.HELMET, ItemStack.AIR,
            EquipmentSlot.CHESTPLATE, ItemStack.AIR,
            EquipmentSlot.LEGGINGS, ItemStack.AIR,
            EquipmentSlot.BOOTS, ItemStack.AIR
        ));
        Map<Integer, UUID> idents = new HashMap<>();
        Set<Integer> hasJoinEvent = new HashSet<>();
        Map<Integer, Map<EquipmentSlot, ItemStack>> previousEquippedItems = new HashMap<>();
        // elements here need to be removed for deduplication or otherwise
        List<Integer> removalIndices = new ArrayList<>();
        Map<Integer, ReplayEvent> replacementIndices = new HashMap<>();
        for (int i = 0; i < events.size(); i++) {
            ReplayEvent event = events.get(i);
            if (event instanceof PlayerIdent e) {
                removalIndices.add(i);
                idents.putAll(e.idents());
                continue;
            }

            if (event instanceof PlayerJoin e) {
                hasJoinEvent.add(e.entityId());
                continue;
            }

            if (event instanceof BlockUpdate(int tick1, Point pos1, Block block1, Block existing1)) {
                if (i == 0) continue;
                ReplayEvent prev = events.get(i - 1);
                if (!(prev instanceof BlockUpdate(int tick2, Point pos2, Block block2, Block existing2))) continue;
                if (tick2 != tick1) continue;
                if (!pos1.samePoint(pos2)) continue;

                if (existing1 == null && block1.compare(block2)) {
                    // this is likely the instance update event with more accurate state
                    replacementIndices.put(i, new BlockUpdate(tick1, pos1, block1, existing2));
                }
                // dedupe the older one regardless
                removalIndices.add(i - 1);

                // if block updates happen in the same tick, and same pos, keep the one that has an existing block.
                // this is because the Player events have existing block information, while the instance event does not.
                // The instance event has more accurate block states, since it is called after block handlers, so the
                // events should be merged together if available.
                // this means that blocks created by the minestom API may not have accurate inverses (if the blocks changed
                // were not air.)
            }

            if (event instanceof EquipmentUpdate e) {
                if (i == 0) continue;
                Map<EquipmentSlot, ItemStack> prev = previousEquippedItems
                    .computeIfAbsent(e.entityId(), _ -> new HashMap<>(defaultEquipment));
                AtomicBoolean isChange = new AtomicBoolean(false);
                e.slots().forEach((s, item) -> {
                    ItemStack old = prev.getOrDefault(s, ItemStack.AIR);
                    if (!old.isSimilar(item)) {
                        isChange.set(true);
                    }
                });
                if (!isChange.get()) {
                    removalIndices.add(i);
                    continue;
                }
                prev.putAll(e.slots());
            }
        }

        replacementIndices.forEach(events::set);

        for (int i = removalIndices.size() - 1; i >= 0; i--) {
            events.remove((int) removalIndices.get(i));  // these are extracted to ident map
        }

        Set<Integer> missingJoin = new HashSet<>(idents.keySet());
        missingJoin.removeAll(hasJoinEvent);
        if (!missingJoin.isEmpty()) { // some players need join events to be injected
            int tick = events.getFirst().tick();
            for (Integer i : missingJoin) {
                Logger.warn("Injecting player into bundle: %d", i);
                events.addFirst(new PlayerJoin(tick, i));
            }
        }

        Logger.debug("Stripped %d events during normalization.", eventsStart - events.size());

        return idents;
    }
}
