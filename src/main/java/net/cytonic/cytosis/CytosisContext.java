package net.cytonic.cytosis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.objects.ServerGroup;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

/**
 * Holds references to all Cytosis components for dependency passing.
 */
@Getter
@Setter
public class CytosisContext {

    // The instance ID is used to identify the server
    public static final String SERVER_ID = generateID();
    private ServerGroup serverGroup = new ServerGroup("cytonic", "lobby");

    // map for bootstrappable services
    private Map<Class<?>, Object> components = new HashMap<>();

    // consumers that should run when a component becomes available
    private Map<Class<?>, List<Consumer<?>>> availabilityConsumers = new HashMap<>();

    // Misc
    private List<String> flags;
    private boolean metricsEnabled = false;

    /**
     * Retrieves a component of the specified class type from the context.
     *
     * @param <T>   the type of the component to retrieve
     * @param clazz the class of the component to retrieve
     * @return the instance of the requested component if it is registered, or null if the component does not exist
     */
    public <T> T getComponent(Class<T> clazz) {
        return getComponent(clazz, false);
    }

    /**
     * Retrieves a component of the specified class type from the context.
     *
     * @param <T>             the type of the component to retrieve
     * @param clazz           the class type of the component to retrieve
     * @param createIfMissing a flag indicating if a missing component should be created and registered
     * @return the instance of the requested component, or null if it does not exist and createIfMissing is false
     */
    public <T> T getComponent(Class<T> clazz, boolean createIfMissing) {
        Object existing = components.get(clazz);
        if (existing != null) {
            return clazz.cast(existing);
        }
        if (!createIfMissing) {
            return null;
        }
        return registerComponent(clazz);
    }

    /**
     * Registers a component of the specified class type.
     *
     * @param <T>   the type of the component being registered
     * @param clazz the class of the component to be registered, which must have a no-arg constructor
     * @return the registered instance of the component
     * @throws RuntimeException if the component instance cannot be created
     */
    public <T> T registerComponent(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            return registerComponent(clazz, instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create component: " + clazz.getName(), e);
        }
    }

    /**
     * Registers a component in the context. If a component with the specified class type is already registered, the
     * existing instance is returned. If the new component implements {@code Bootstrappable}, its {@code init()} method
     * is invoked during the registration process. Additionally, notifies all registered consumers that this component
     * is now available.
     *
     * @param <T>       the type of the component being registered
     * @param key       the class type or a superclass of the component
     * @param component the component instance to be registered
     * @return the registered component instance, or the existing instance if one is already registered
     */
    public <T> T registerComponent(Class<? super T> key, T component) {
        Object existing = components.putIfAbsent(key, component);
        if (existing == null) {
            if (component instanceof Bootstrappable bootstrappableComponent) {
                bootstrappableComponent.init();
            }

            notifyAvailable(key, component);
            return component;
        }

        return (T) existing;
    }

    /**
     * Registers a given component to the context.
     *
     * @param <T>       the type of the component being registered
     * @param component the component instance to register
     * @return the registered component instance if it was not already registered, or the existing instance if already
     * present
     */
    public <T> T registerComponent(T component) {
        return registerComponent((Class<? super T>) component.getClass(), component);
    }

    /**
     * Register a callback to be executed once the requested component is registered. If the component is already
     * registered, the consumer is executed immediately.
     */
    public <T> void whenAvailable(Class<T> componentClass, Consumer<T> consumer) {
        T existing = getComponent(componentClass);
        if (existing != null) {
            consumer.accept(existing);
            return;
        }
        availabilityConsumers
            .computeIfAbsent(componentClass, k -> new ArrayList<>())
            .add(consumer);
    }

    /**
     * Notifies all registered consumers that a specific component is now available.
     *
     * @param componentClass the class type of the component that has become available
     * @param instance       the instance of the component that is now available passed to consumers
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void notifyAvailable(Class<?> componentClass, Object instance) {
        List<Consumer<?>> listeners = availabilityConsumers.remove(componentClass);
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (Consumer consumer : listeners) {
            try {
                consumer.accept(instance);
            } catch (ClassCastException ignored) {
            }
        }
    }

    public CytonicServer currentServer() {
        return new CytonicServer(Utils.getServerIP(), SERVER_ID, CytosisSettings.SERVER_PORT, getServerGroup().type(),
            getServerGroup().group());
    }

    /**
     * Generates a random Server ID:
     * <p>
     * TODO: make a check for existing server ids
     *
     * @return a random Server ID
     */
    private static String generateID() {
        var rnd = new java.security.SecureRandom();
        char first = (char) ('a' + rnd.nextInt(26));
        char last = (char) ('a' + rnd.nextInt(26));
        int mid = rnd.nextInt(100000); // 0..99999
        return "%c%05d%c".formatted(first, mid, last); // e.g., a01234b
    }

    public void shutdownHandler() {
        Cytosis.getOnlinePlayers()
            .forEach(onlinePlayer -> onlinePlayer.kick(Msg.mm("<red>The server is shutting down.")));

        // shutdown bootstrappable components
        components.values().stream().filter(component -> component instanceof Bootstrappable)
            .map(Bootstrappable.class::cast).forEach(Bootstrappable::shutdown);
    }
}