package net.cytonic.cytosis;

import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.objects.ServerGroup;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Misc
    private List<String> flags;
    private boolean metricsEnabled = false;

    public <T> T getComponent(Class<T> clazz) {
        return getComponent(clazz, false);
    }

    public <T> T getComponent(Class<T> clazz, boolean createIfMissing) {
        Object existing = components.get(clazz);
        if (existing != null) return clazz.cast(existing);
        if (!createIfMissing) return null;
        return registerComponent(clazz);
    }

    public <T> T registerComponent(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            return registerComponent(clazz, instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create component: " + clazz.getName(), e);
        }
    }

    public <T> T registerComponent(Class<? super T> key, T component) {
        Object existing = components.putIfAbsent(key, component);
        if (existing == null) {
            if (component instanceof Bootstrappable bootstrappableComponent)
                bootstrappableComponent.init();

            return component;
        }

        return (T) existing;
    }

    public <T> T registerComponent(T component) {
        return registerComponent((Class<? super T>) component.getClass(), component);
    }

    public CytonicServer currentServer() {
        return new CytonicServer(Utils.getServerIP(), SERVER_ID, CytosisSettings.SERVER_PORT, getServerGroup().type(), getServerGroup().group());
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
        Cytosis.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.kick(Msg.mm("<red>The server is shutting down.")));

        // shutdown bootstrappable components
        components.values().stream()
                .filter(component -> component instanceof Bootstrappable bootstrappableComponent)
                .map(Bootstrappable.class::cast)
                .forEach(Bootstrappable::shutdown);
    }
}