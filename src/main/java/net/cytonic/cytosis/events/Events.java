package net.cytonic.cytosis.events;

import lombok.experimental.UtilityClass;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.utils.events.PlayerJoinEventResponse;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A ultility class to increase development speed by creating a simple consumer for
 * common use events. If order is really important, use a tradicional event listener.
 * The events contained in this class run at a priority of 50, which is fairly low as
 * event priorities go. If the event is async by nature, ie {@link AsyncPlayerConfigurationEvent},
 * then the event will be run off of ticking threads. Otherwise, the even will be run
 * on ticking threads.
 */
@SuppressWarnings({"unused", "unchecked"})
@UtilityClass
public final class Events {
    private static final List<Consumer<AsyncPlayerConfigurationEvent>> spawnConsumers = new ArrayList<>();
    private static final List<Consumer<PlayerDisconnectEvent>> disconnectConsumers = new ArrayList<>();


    static {
        Cytosis.getEventHandler().registerListeners(
                new EventListener<>(AsyncPlayerConfigurationEvent.class,
                        event -> spawnConsumers.forEach(eventListenerConsumer -> eventListenerConsumer.accept(event)),
                        false, 50, "cytosis:events_util_join", true),
                new EventListener<>(PlayerDisconnectEvent.class,
                        event -> disconnectConsumers.forEach(eventListenerConsumer -> eventListenerConsumer.accept(event)),
                        false, 50, "cytosis:events_util_leave", true)
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
}
