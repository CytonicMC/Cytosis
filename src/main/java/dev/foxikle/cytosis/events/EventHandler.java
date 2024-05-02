package dev.foxikle.cytosis.events;

import net.minestom.server.event.Event;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.trait.CancellableEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * EventHandler class is responsible for handling events and managing listeners.
 * It provides methods to register, unregister listeners and to handle global events.
 *
 * @author Foxikle
 */
public class EventHandler {

    private final GlobalEventHandler GLOBAL_HANDLER;
    private final Map<String, EventListener<Event>> NAMESPACED_HANDLERS = new HashMap<>();

    /**
     * Constructor for EventHandler.
     * Initializes the GlobalEventHandler instance.
     *
     * @param globalHandler The GlobalEventHandler instance to be used.
     */
    public EventHandler(GlobalEventHandler globalHandler) {
        GLOBAL_HANDLER = globalHandler;
    }

    /**
     * Unregisters a listener by its namespace.
     *
     * @param listener The listener to be unregistered.
     * @return True if the listener was successfully unregistered, false otherwise.
     */
    public boolean unregisterListener(EventListener<Event> listener) {
        return NAMESPACED_HANDLERS.remove(listener.getNamespace()) != null;
    }

    /**
     * Unregisters a listener by its namespace.
     *
     * @param namespace The namespace of the listener to be unregistered.
     * @return True if the listener was successfully unregistered, false otherwise.
     */
    public boolean unregisterListener(String namespace) {
        return NAMESPACED_HANDLERS.remove(namespace) != null;
    }

    /**
     * Registers a listener.
     *
     * @param listener The listener to be registered.
     * @return True if the listener was successfully registered, false otherwise.
     */
    public boolean registerListener(EventListener<Event> listener) {
        return NAMESPACED_HANDLERS.putIfAbsent(listener.getNamespace(), listener) == listener;
    }

    public void handleEvent(Event event) {
        NAMESPACED_HANDLERS.values().forEach(listener -> {
            if (event.getClass() == listener.getEventClass()) {
                if (event instanceof CancellableEvent cancellableEvent && cancellableEvent.isCancelled()) {
                    return;
                }
                listener.complete(event);
            }
        });
    }
}
