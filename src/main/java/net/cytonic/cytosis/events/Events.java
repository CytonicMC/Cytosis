package net.cytonic.cytosis.events;

import lombok.experimental.UtilityClass;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.network.PlayerJoinNetworkEvent;
import net.cytonic.cytosis.events.network.PlayerLeaveNetworkEvent;
import net.cytonic.cytosis.utils.events.PlayerJoinEventResponse;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A ultility class to increase development speed by creating a simple consumer for
 * common use events. If order is really important, use a tradicional event listener.
 * The events contained in this class run at a priority of 50, which is fairly low as
 * event priorities go. If the event is async by nature, ie {@link AsyncPlayerConfigurationEvent},
 * then the event will be run off of ticking threads. Otherwise, the even will be run
 * on ticking threads. These handlers ignore if the event has already been cancelled.
 */
@SuppressWarnings({"unused", "unchecked"})
@UtilityClass
public final class Events {
    private static final List<Consumer<AsyncPlayerConfigurationEvent>> spawnConsumers = new ArrayList<>();
    private static final List<Consumer<PlayerDisconnectEvent>> disconnectConsumers = new ArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> packetOut = new ArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> packetIn = new ArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> packetOutHigh = new ArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> packetInHigh = new ArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> packetOutLow = new ArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> packetInLow = new ArrayList<>();
    public static final List<Consumer<PlayerLeaveNetworkEvent>> networkLeave = new ArrayList<>();
    public static final List<Consumer<PlayerJoinNetworkEvent>> networkJoin = new ArrayList<>();

    static {
        Cytosis.getEventHandler().registerListeners(
                new EventListener<>(AsyncPlayerConfigurationEvent.class,
                        event -> spawnConsumers.forEach(consumer -> consumer.accept(event)),
                        false, 50, "cytosis:events_util_join", true),
                new EventListener<>(PlayerDisconnectEvent.class,
                        event -> disconnectConsumers.forEach(consumer -> consumer.accept(event)),
                        false, 50, "cytosis:events_util_leave", true),
                // medium prio
                new EventListener<>(PlayerPacketEvent.class,
                        event -> packetIn.forEach(consumer -> consumer.accept(event)),
                        false, 50, "cytosis:events_util_packet_in", true),
                new EventListener<>(PlayerPacketOutEvent.class,
                        event -> packetOut.forEach(consumer -> consumer.accept(event)),
                        false, 50, "cytosis:events_util_packet_out", true),
                // low prio
                new EventListener<>(PlayerPacketEvent.class,
                        event -> packetInLow.forEach(consumer -> consumer.accept(event)),
                        false, 100, "cytosis:events_util_packet_in_low", true),
                new EventListener<>(PlayerPacketOutEvent.class,
                        event -> packetOutLow.forEach(consumer -> consumer.accept(event)),
                        false, 100, "cytosis:events_util_packet_out_low", true),
                // high prio
                new EventListener<>(PlayerPacketEvent.class,
                        event -> packetInHigh.forEach(consumer -> consumer.accept(event)),
                        false, 0, "cytosis:events_util_packet_in_high", true),
                new EventListener<>(PlayerPacketOutEvent.class,
                        event -> packetOutHigh.forEach(consumer -> consumer.accept(event)),
                        false, 0, "cytosis:events_util_packet_out_high", true),
                new EventListener<>(PlayerJoinNetworkEvent.class,
                        event -> networkJoin.forEach(consumer -> consumer.accept(event)),
                        false, 0, "cytosis:events_util_network_join", true),
                new EventListener<>(PlayerLeaveNetworkEvent.class,
                        event -> networkLeave.forEach(consumer -> consumer.accept(event)),
                        false, 0, "cytosis:events_util_network_leave", true)
        );
    }

    /**
     * A simplified wrapper around {@link #onJoinRaw(Consumer)} providing the
     * player object when a player joins. The player has not yet spawned into an instance at the time of calling this event.
     *
     * @param eventConsumer the consumer consuming the player joining
     */
    public static void onJoin(Consumer<Player> eventConsumer) {
        spawnConsumers.add(event -> eventConsumer.accept(event.getPlayer()));
    }

    /**
     * A simplified wrapper around {@link #onJoinRaw(Consumer)} providing more
     * flexibility than {@link #onJoin(Consumer)} with a functional interface providing more than just a player --an intance.
     *
     * @param response The functional interface to be called on the execution of the event
     */
    public static void onJoin(PlayerJoinEventResponse response) {
        spawnConsumers.add(event -> response.accept(event.getPlayer(), event.getSpawningInstance()));
    }

    /**
     * Adds a tracked consumer that is <strong>always</strong> executed, synchronously. This is still
     * called if the event is cancelled. This is called on the {@link AsyncPlayerConfigurationEvent} event.
     *
     * @param event The consumer, consuming the entire event object.
     */
    public static void onJoinRaw(Consumer<AsyncPlayerConfigurationEvent> event) {
        spawnConsumers.add(event);
    }

    /**
     * A simplified wrapper around {@link Events#onLeaveRaw(Consumer)}. It is still always called, even if
     * the event is cancelled. This is always run synchronously.
     * This is called on the {@link PlayerDisconnectEvent} event.
     *
     * @param eventConsumer The consumer consuming the player leaving.
     */
    public static void onLeave(Consumer<Player> eventConsumer) {
        disconnectConsumers.add(event -> eventConsumer.accept(event.getPlayer()));
    }

    /**
     * Adds a tracked consumer that is <strong>always</strong> executed, synchronously. This is still called if the event is cancelled.
     * This is called on the {@link PlayerDisconnectEvent} event.
     *
     * @param event The consumer, consuming the entire event object.
     */
    public static void onLeaveRaw(Consumer<PlayerDisconnectEvent> event) {
        disconnectConsumers.add(event);
    }

    /**
     * Registers a consumer that will be executed whenever a player packet is received.
     * This method adds the given consumer to the internal packet input handler.
     *
     * @param event The consumer that processes the incoming player packet event.
     */
    public static void onPacketIn(Consumer<PlayerPacketEvent> event) {
        packetIn.add(event);
    }

    /**
     * Registers a consumer that will be executed whenever a packet is sent to a player.
     * This method adds the given consumer to the internal packet output handler.
     *
     * @param event The consumer that processes the outgoing player packet event.
     */
    public static void onPacketOut(Consumer<PlayerPacketOutEvent> event) {
        packetOut.add(event);
    }

    /**
     * Registers a consumer that will be executed with high priority whenever a player packet is received.
     * This method adds the given consumer to the internal handler for incoming packets with high priority.
     *
     * @param event The consumer that processes the incoming player packet event with high priority.
     */
    public static void onPacketInHighPriority(Consumer<PlayerPacketEvent> event) {
        packetInHigh.add(event);
    }

    /**
     * Registers a consumer that will be executed with high priority whenever a packet
     * is sent to a player. This method adds the given consumer to the internal handler
     * for outgoing packets with high priority.
     *
     * @param event The consumer that processes the outgoing player packet event with high priority.
     */
    public static void onPacketOutHighPriority(Consumer<PlayerPacketOutEvent> event) {
        packetOutHigh.add(event);
    }

    /**
     * Registers a consumer that will be executed with low priority whenever a player packet is received.
     * This method adds the given consumer to the internal handler for incoming packets with low priority.
     *
     * @param event The consumer that processes the incoming player packet event with low priority.
     */
    public static void onPacketInLowPriority(Consumer<PlayerPacketEvent> event) {
        packetInLow.add(event);
    }

    /**
     * Registers a consumer that will be executed with low priority whenever a packet
     * is sent to a player. This method adds the given consumer to the internal handler
     * for outgoing packets with low priority.
     *
     * @param event The consumer that processes the outgoing player packet event with low priority.
     */
    public static void onPacketOutLowPriority(Consumer<PlayerPacketOutEvent> event) {
        packetOutLow.add(event);
    }


    /**
     * Registers a consumer that will be executed when a player joins the network.
     * This method adds the given consumer to the internal handler for network join events.
     *
     * @param event The consumer that processes the player join network event.
     */
    public static void onNetworkJoin(Consumer<PlayerJoinNetworkEvent> event) {
        networkJoin.add(event);
    }


    /**
     * Registers a consumer that will be executed when a player leaves the network.
     * This method adds the given consumer to the internal handler for network leave events.
     *
     * @param event The consumer that processes the player leave network event.
     */
    public static void onNetworkLeave(Consumer<PlayerLeaveNetworkEvent> event) {
        networkLeave.add(event);
    }


}
