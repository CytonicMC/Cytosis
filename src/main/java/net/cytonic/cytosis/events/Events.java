package net.cytonic.cytosis.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoadedEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;

import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.events.network.PlayerJoinNetworkEvent;
import net.cytonic.cytosis.events.network.PlayerLeaveNetworkEvent;
import net.cytonic.cytosis.utils.events.PlayerJoinEventResponse;

/**
 * A utility class to increase development speed by creating a simple consumer for common use events. If order is really
 * important, use a traditional event listener. The events contained in this class run at a priority of 50, which is
 * fairly low as event priorities go. If the event is async by nature, ie {@link AsyncPlayerConfigurationEvent}, then
 * the event will be run off of ticking threads. Otherwise, the even will be run on ticking threads. These handlers
 * ignore if the event has already been cancelled.
 */
@SuppressWarnings({"unused", "UnstableApiUsage"})
public class Events {

    private static final List<Consumer<PlayerLeaveNetworkEvent>> NETWORK_LEAVE = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerJoinNetworkEvent>> NETWORK_JOIN = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerMoveEvent>> MOVE = new CopyOnWriteArrayList<>();
    private static final List<Consumer<AsyncPlayerConfigurationEvent>> CONFIG = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerLoadedEvent>> JOIN = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerDisconnectEvent>> DISCONNECT = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> PACKET_OUT = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> PACKET_IN = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> PACKET_OUT_HIGH = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> PACKET_IN_HIGH = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> PACKET_OUT_LOW = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> PACKET_IN_LOW = new CopyOnWriteArrayList<>();

    private Events() {

    }

    /**
     * A simplified wrapper around {@link #onConfigRaw(Consumer)} providing the player object when a player joins. The
     * player has not yet spawned into an instance at the time of calling this event.
     *
     * @param eventConsumer the consumer consuming the player joining
     */
    public static void onConfig(Consumer<Player> eventConsumer) {
        CONFIG.add(event -> eventConsumer.accept(event.getPlayer()));
    }

    /**
     * A simplified wrapper around {@link #onConfigRaw(Consumer)} providing more flexibility than
     * {@link #onConfig(Consumer)} with a functional interface providing more than just a player --an instance.
     *
     * @param response The functional interface to be called on the execution of the event
     */
    public static void onConfig(PlayerJoinEventResponse response) {
        CONFIG.add(event -> response.accept(event.getPlayer(), event.getSpawningInstance()));
    }

    /**
     * Registers a consumer to be executed when a player joins. This method adds the specified consumer to handle the
     * event, providing the {@link Player} object associated with the joining player. This is called on the
     * {@link PlayerLoadedEvent}, and this method is a wrapper around the {@link #onJoinRaw(Consumer)}.
     *
     * @param eventConsumer the consumer that processes the {@link Player} object when a player joins
     */
    public static void onJoin(Consumer<Player> eventConsumer) {
        JOIN.add(event -> eventConsumer.accept(event.getPlayer()));
    }

    /**
     * Registers a consumer to be executed when a player has fully loaded and joined. This method adds the given
     * consumer to handle the {@link PlayerLoadedEvent}.
     *
     * @param eventConsumer the consumer that processes the event fired when a player has completed loading and joined
     */
    public static void onJoinRaw(Consumer<PlayerLoadedEvent> eventConsumer) {
        JOIN.add(eventConsumer);
    }

    /**
     * Adds a tracked consumer that is <strong>always</strong> executed, synchronously. This is still called if the
     * event is cancelled. This is called on the {@link AsyncPlayerConfigurationEvent} event.
     *
     * @param event The consumer, consuming the entire event object.
     */
    public static void onConfigRaw(Consumer<AsyncPlayerConfigurationEvent> event) {
        CONFIG.add(event);
    }

    /**
     * A simplified wrapper around {@link Events#onLeaveRaw(Consumer)}. It is still always called, even if the event is
     * cancelled. This is always run synchronously. This is called on the {@link PlayerDisconnectEvent} event.
     *
     * @param eventConsumer The consumer consuming the player leaving.
     */
    public static void onLeave(Consumer<Player> eventConsumer) {
        DISCONNECT.add(event -> eventConsumer.accept(event.getPlayer()));
    }

    /**
     * Adds a tracked consumer that is <strong>always</strong> executed, synchronously. This is still called if the
     * event is cancelled. This is called on the {@link PlayerDisconnectEvent} event.
     *
     * @param event The consumer, consuming the entire event object.
     */
    public static void onLeaveRaw(Consumer<PlayerDisconnectEvent> event) {
        DISCONNECT.add(event);
    }

    /**
     * Registers a consumer that will be executed whenever a player packet is received. This method adds the given
     * consumer to the internal packet input handler.
     *
     * @param event The consumer that processes the incoming player packet event.
     */
    public static void onPacketIn(Consumer<PlayerPacketEvent> event) {
        PACKET_IN.add(event);
    }

    /**
     * Registers a consumer that will be executed whenever a packet is sent to a player. This method adds the given
     * consumer to the internal packet output handler.
     *
     * @param event The consumer that processes the outgoing player packet event.
     */
    public static void onPacketOut(Consumer<PlayerPacketOutEvent> event) {
        PACKET_OUT.add(event);
    }

    /**
     * Registers a consumer that will be executed with high priority whenever a player packet is received. This method
     * adds the given consumer to the internal handler for incoming packets with high priority.
     *
     * @param event The consumer that processes the incoming player packet event with high priority.
     */
    public static void onPacketInHighPriority(Consumer<PlayerPacketEvent> event) {
        PACKET_IN_HIGH.add(event);
    }

    /**
     * Registers a consumer that will be executed with high priority whenever a packet is sent to a player. This method
     * adds the given consumer to the internal handler for outgoing packets with high priority.
     *
     * @param event The consumer that processes the outgoing player packet event with high priority.
     */
    public static void onPacketOutHighPriority(Consumer<PlayerPacketOutEvent> event) {
        PACKET_OUT_HIGH.add(event);
    }

    /**
     * Registers a consumer that will be executed with low priority whenever a player packet is received. This method
     * adds the given consumer to the internal handler for incoming packets with low priority.
     *
     * @param event The consumer that processes the incoming player packet event with low priority.
     */
    public static void onPacketInLowPriority(Consumer<PlayerPacketEvent> event) {
        PACKET_IN_LOW.add(event);
    }

    /**
     * Registers a consumer that will be executed with low priority whenever a packet is sent to a player. This method
     * adds the given consumer to the internal handler for outgoing packets with low priority.
     *
     * @param event The consumer that processes the outgoing player packet event with low priority.
     */
    public static void onPacketOutLowPriority(Consumer<PlayerPacketOutEvent> event) {
        PACKET_OUT_LOW.add(event);
    }

    /**
     * Registers a consumer that will be executed when a player joins the network. This method adds the given consumer
     * to the internal handler for network join events.
     *
     * @param event The consumer that processes the player join network event.
     */
    public static void onNetworkJoin(Consumer<PlayerJoinNetworkEvent> event) {
        NETWORK_JOIN.add(event);
    }

    /**
     * Registers a consumer that will be executed when a player leaves the network. This method adds the given consumer
     * to the internal handler for network leave events.
     *
     * @param event The consumer that processes the player leave network event.
     */
    public static void onNetworkLeave(Consumer<PlayerLeaveNetworkEvent> event) {
        NETWORK_LEAVE.add(event);
    }

    public static void unregisterAll(ClassLoader classLoader) {
        NETWORK_LEAVE.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        NETWORK_JOIN.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        MOVE.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        CONFIG.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        JOIN.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        DISCONNECT.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        PACKET_OUT.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        PACKET_IN.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        PACKET_OUT_HIGH.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        PACKET_IN_HIGH.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        PACKET_OUT_LOW.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
        PACKET_IN_LOW.removeIf(consumer -> consumer.getClass().getClassLoader() == classLoader);
    }

    public static void onMove(Consumer<PlayerMoveEvent> consumer) {
        MOVE.add(consumer);
    }

    @Listener
    public void onEvent(final PlayerJoinNetworkEvent event) {
        NETWORK_JOIN.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    public void onEvent(final PlayerLeaveNetworkEvent event) {
        NETWORK_LEAVE.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    public void onEvent(final PlayerLoadedEvent event) {
        JOIN.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final AsyncPlayerConfigurationEvent event) {
        CONFIG.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final PlayerDisconnectEvent event) {
        DISCONNECT.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final PlayerPacketEvent event) {
        PACKET_IN.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final PlayerPacketOutEvent event) {
        PACKET_OUT.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final PlayerMoveEvent event) {
        MOVE.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    @Priority(100)
    private void onEventLow(final PlayerPacketEvent event) {
        PACKET_IN_LOW.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    @Priority(100)
    private void onEventLow(final PlayerPacketOutEvent event) {
        PACKET_OUT_LOW.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    @Priority(0)
    private void onEventHigh(final PlayerPacketEvent event) {
        PACKET_IN_HIGH.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    @Priority(0)
    private void onEventHigh(final PlayerPacketOutEvent event) {
        PACKET_OUT_HIGH.forEach(consumer -> consumer.accept(event));
    }
}
