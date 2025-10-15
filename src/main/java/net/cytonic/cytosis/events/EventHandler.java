package net.cytonic.cytosis.events;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.classgraph.ClassGraph;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.ApiStatus;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;

/**
 * EventHandler class is responsible for handling events and managing listeners. It provides methods to register,
 * unregister listeners and to handle global events.
 *
 * @author Foxikle
 */
@CytosisComponent(dependsOn = {MinecraftServer.class})
public class EventHandler {

    private GlobalEventHandler globalEventHandler;
    private final Map<String, EventListener<? extends Event>> namespacedHandlers = new HashMap<>();
    private boolean initialized = false;

    public EventHandler() {
    }

    public void findEvents() {
        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Cytosis.class.getClassLoader());
        loaders.addAll(PluginClassLoader.LOADERS);

        final ClassGraph classGraph = new ClassGraph().acceptPackages("net.minestom", "net.cytonic",
                "io.github.togar2") // cytonic things, and PVP
            .enableAllInfo()
            .overrideClassLoaders(loaders.toArray(new ClassLoader[0]));

        classGraph.scan().getClassesImplementing(Event.class.getName()).forEach(classInfo -> {
            Class<?> clazz = classInfo.loadClass();
            globalEventHandler.addListener(clazz.asSubclass(Event.class), this::handleEvent);
        });

    }

    /**
     * Initializes the event handler.
     *
     * @throws IllegalStateException if the event handler has already been initialized.
     */
    public void init() {
        if (initialized) throw new IllegalStateException("The event handler has already been initialized!");
        this.globalEventHandler = MinecraftServer.getGlobalEventHandler();
        findEvents();
        initialized = true;
    }

    /**
     * Handles the specified event
     *
     * @param event The event object
     * @param <T>   The type of the event
     */
    private <T extends Event> void handleEvent(T event) {
        List<EventListener<? extends Event>> matchingListeners = new ArrayList<>();
        for (EventListener<? extends Event> listener : namespacedHandlers.values()) {
            if (listener.getEventClass() == event.getClass()) {
                matchingListeners.add(listener);
            }
        }
        // Sort listeners by priority
        matchingListeners.sort(Comparator.comparingInt(EventListener::getPriority));

        for (EventListener<? extends Event> listener : matchingListeners) {
            if (listener.isIgnoreCancelled()) {
                completeEvent(event, listener);
                continue;
            }
            if (event instanceof CancellableEvent && ((CancellableEvent) event).isCancelled()) {
                // the event has been cancelled, future listeners get skipped over
                continue;
            }
            completeEvent(event, listener);
        }
    }

    private void completeEvent(Event event, EventListener<? extends Event> listener) {
        if (listener.isAsync()) {
            Thread.ofVirtual().name("Cytosis-Event-thread-", 1).start(() -> listener.complete(event));
            return;
        }
        listener.complete(event);
    }

    /**
     * Registers a listener.
     *
     * @param listener The listener to be registered.
     */
    @ApiStatus.Internal
    public void registerListener(EventListener<? extends Event> listener) {
        namespacedHandlers.putIfAbsent(listener.getNamespace(), listener);
    }
}