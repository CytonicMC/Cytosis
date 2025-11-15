package net.cytonic.cytosis.events;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.event.Event;

/**
 * A class that represents an event listener. It contains information about the event class, priority, and consumer.
 *
 * @param <T> The event class type
 */
@Getter
@AllArgsConstructor
public class EventListener<T extends Event> {

    private final Class<T> eventClass;
    private final boolean async;
    private final int priority;
    private final String namespace;
    private final boolean ignoreCancelled;
    @Setter
    private Consumer<T> consumer = null;

    /**
     * Constructs a new instance of {@link EventListener} with the specified namespace, priority, and consumer.
     *
     * @param namespace  The namespace of the event listener.
     * @param eventClass The class of the event that the listener will be triggered for.
     * @param async      A boolean value indicating whether the event listener should run asynchronously.
     * @param priority   The priority of the event listener.
     * @param consumer   The consumer that will be called when the event is triggered.
     * @since 1.0.0
     */
    public EventListener(String namespace, boolean async, int priority, Class<T> eventClass, Consumer<T> consumer) {
        this.consumer = consumer;
        this.eventClass = eventClass;
        this.async = async;
        this.priority = priority;
        this.namespace = namespace;
        this.ignoreCancelled = false;
    }

    /**
     * Constructs a new instance of {@link EventListener} with the specified namespace, priority, and consumer. It will
     * be executed synchronously.
     *
     * @param namespace  The namespace of the event listener.
     * @param priority   The priority of the event listener.
     * @param consumer   The consumer that will be called when the event is triggered.
     * @param eventClass The class of the event that the listener will be triggered for.
     * @since 1.0.0
     */
    public EventListener(String namespace, int priority, Class<T> eventClass, Consumer<T> consumer) {
        this.consumer = consumer;
        this.async = false;
        this.eventClass = eventClass;
        this.priority = priority;
        this.namespace = namespace;
        this.ignoreCancelled = false;
    }

    /**
     * Constructs a new instance of {@link EventListener} with the specified namespace and priority.
     *
     * @param namespace  The namespace of the event listener.
     * @param eventClass The class of the event that the listener will be triggered for.
     * @param async      A boolean value indicating whether the event listener should run asynchronously.
     * @param priority   The priority of the event listener.
     * @since 1.0.0
     */
    public EventListener(String namespace, boolean async, int priority, Class<T> eventClass) {
        this.eventClass = eventClass;
        this.async = async;
        this.priority = priority;
        this.namespace = namespace;
        this.ignoreCancelled = false;
    }

    /**
     * Constructs a new instance of {@link EventListener} with the specified namespace and priority. It will be executed
     * synchronously.
     *
     * @param namespace  The namespace of the event listener.
     * @param priority   The priority of the event listener.
     * @param eventClass The class of the event that the listener will be triggered for.
     * @since 1.0.0
     */
    public EventListener(String namespace, int priority, Class<T> eventClass) {
        this.async = false;
        this.eventClass = eventClass;
        this.priority = priority;
        this.namespace = namespace;
        this.ignoreCancelled = false;
    }

    /**
     * Completes the EventListener's consumer with the provided event object.
     *
     * @param event The event to complete the consumer with.
     * @since 1.0.0
     */
    public void complete(Object event) {
        if (!eventClass.isInstance(event)) {
            throw new IllegalArgumentException(
                "The specified event object isn't an instance of " + eventClass.getName());
        }
        consumer.accept(eventClass.cast(event));
    }
}