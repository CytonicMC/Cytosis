package net.cytonic.cytosis.events;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class EventHandler {
    private final GlobalEventHandler GLOBAL_HANDLER;

    public EventHandler(GlobalEventHandler globalHandler) {
        GLOBAL_HANDLER = globalHandler;
    }

    public <T extends Event> EventNode<Event> registerGlobalEvent(EventListener<T> listener) {
        return GLOBAL_HANDLER.addListener(listener);
    }

    public <E extends Event> @NotNull EventNode<Event> registerGlobalEvent(@NotNull Class<E> clazz, @NotNull Consumer<E> listener) {
        return GLOBAL_HANDLER.addListener(EventListener.of(clazz, listener));
    }
}