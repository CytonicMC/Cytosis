package net.cytonic.cytosis.managers;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.play.ActionBarPacket;
import net.minestom.server.timer.TaskSchedule;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.events.Events;
import net.cytonic.cytosis.utils.ActionbarSupplier;

/**
 * The class that handles actionbar sending to players
 */
@NoArgsConstructor
@CytosisComponent(dependsOn = {NetworkCooldownManager.class})
public class ActionbarManager implements Bootstrappable {

    private static final int DEFAULT_TICKS = 20;

    // Scheduled queue per player
    private final Map<UUID, Queue<MessageEntry>> messageQueues = new ConcurrentHashMap<>();
    // Immediate-priority queue per player
    private final Map<UUID, Queue<MessageEntry>> immediateQueues = new ConcurrentHashMap<>();
    // Currently active message per player
    private final Map<UUID, MessageEntry> currentMessages = new ConcurrentHashMap<>();

    private final Set<UUID> cooldowns = ConcurrentHashMap.newKeySet();
    @Setter
    @Getter
    private ActionbarSupplier defaultSupplier = ActionbarSupplier.DEFAULT;

    /**
     * Sets up the manager, registering event listeners, and starting the loop.
     */
    @Override
    public void init() {
        Events.onConfig((player) -> {
            messageQueues.put(player.getUuid(), new ConcurrentLinkedQueue<>());
            immediateQueues.put(player.getUuid(), new ConcurrentLinkedQueue<>());
            cooldowns.add(player.getUuid());
            // prevent sending packets too early
            MinecraftServer.getSchedulerManager().buildTask(() -> cooldowns.remove(player.getUuid()))
                .delay(TaskSchedule.tick(5)).schedule();
        });
        Events.onLeave((player) -> {
            messageQueues.remove(player.getUuid());
            immediateQueues.remove(player.getUuid());
            currentMessages.remove(player.getUuid());
            cooldowns.remove(player.getUuid());
        });

        // Run every tick to support custom durations and immediate overrides
        MinecraftServer.getSchedulerManager()
            .scheduleTask(() -> messageQueues.forEach((uuid, ignored) -> handleQueueForPlayer(uuid)),
                TaskSchedule.nextTick(), TaskSchedule.tick(1));
    }

    private void handleQueueForPlayer(UUID uuid) {
        if (cooldowns.contains(uuid)) {
            return;
        }
        Cytosis.getPlayer(uuid).ifPresentOrElse(p -> {
            MessageEntry active = currentMessages.get(uuid);
            if (active == null || active.remainingTicks <= 0) {
                Queue<MessageEntry> iq = immediateQueues.get(uuid);
                Queue<MessageEntry> nq = messageQueues.get(uuid);
                if (iq != null && !iq.isEmpty()) {
                    active = iq.poll();
                    currentMessages.put(uuid, active);
                } else if (nq != null && !nq.isEmpty()) {
                    active = nq.poll();
                    currentMessages.put(uuid, active);
                } else {
                    // we have to use a packet here to avoid an endless recursion
                    p.sendPacket(new ActionBarPacket(defaultSupplier.getActionbar(p)));
                    return;
                }
            }
            // Send active message and decrement remaining ticks
            if (active != null) {
                // we have to use a packet here to avoid an endless recursion
                p.sendPacket(new ActionBarPacket(active.message));
                active.remainingTicks--;
                if (active.remainingTicks <= 0) {
                    currentMessages.remove(uuid);
                }
            }
        }, () -> {
            messageQueues.remove(uuid);
            immediateQueues.remove(uuid);
            currentMessages.remove(uuid);
            cooldowns.remove(uuid);
        });
    }

    /**
     * Adds the specified message to the queue {@code iterations} times. The message is displayed for the specified
     * number of iterations. If the current queue is empty, then the message is displayed on the next 20 tick interval.
     * Otherwise, the messages are displayed once the queue reaches the messages.
     *
     * @param uuid       The player to send the actionbar to
     * @param message    the message to display
     * @param iterations the number of seconds (20 tick intervals) to display the message for
     */
    public void addToQueue(UUID uuid, Component message, int iterations) {
        // each iteration = 20 ticks
        addToQueueTicks(uuid, message, Math.max(0, iterations) * DEFAULT_TICKS);
    }

    /**
     * Adds a message to the actionbar queue. If the queue is empty, the message is displayed on the next interval.
     *
     * @param uuid    The player to show the message to
     * @param message the message to display
     */
    public void addToQueue(UUID uuid, Component message) {
        addToQueueTicks(uuid, message, DEFAULT_TICKS);
    }

    /**
     * Adds a message to the normal queue with a custom duration in ticks.
     *
     * @param uuid    The player to show the message to
     * @param message the message to display
     * @param ticks   how long to display, in ticks
     */
    public void addToQueueTicks(UUID uuid, Component message, int ticks) {
        if (ticks <= 0) {
            return;
        }
        Queue<MessageEntry> queue = messageQueues.computeIfAbsent(uuid, u -> new ConcurrentLinkedQueue<>());
        queue.add(new MessageEntry(message, ticks));
    }

    /**
     * Adds a message to the immediate-priority queue with a custom duration in ticks. The message is displayed
     * immediately and overrides the standard queue.
     *
     * @param uuid    The player to show the message to
     * @param message the message to display
     * @param ticks   how long to display, in ticks
     */
    public void addImmediate(UUID uuid, Component message, int ticks) {
        if (ticks <= 0) {
            return;
        }
        Queue<MessageEntry> queue = immediateQueues.computeIfAbsent(uuid, u -> new ConcurrentLinkedQueue<>());
        MessageEntry entry = new MessageEntry(message, ticks);
        queue.add(entry);
        currentMessages.put(uuid, entry);
        // we have to use a packet here to avoid an endless recursion
        Cytosis.getPlayer(uuid).ifPresent(p -> p.sendPacket(new ActionBarPacket(message)));
    }

    /**
     * Simple holder for a message with remaining ticks.
     */
    private static final class MessageEntry {

        final Component message;
        int remainingTicks;

        MessageEntry(Component message, int remainingTicks) {
            this.message = message;
            this.remainingTicks = remainingTicks;
        }
    }
}
