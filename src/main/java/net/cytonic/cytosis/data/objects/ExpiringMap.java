package net.cytonic.cytosis.data.objects;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiringMap<V> extends ConcurrentHashMap<UUID, V> {

    // we use a weak reference to ensure that the maps won't be prevented from being GC'd
    private static final Queue<WeakReference<ExpiringMap<?>>> INSTANCES = new ConcurrentLinkedQueue<>();

    private static final Duration EXPIRY_DELAY = Duration.ofSeconds(5);
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "ExpiringMap-expiry");
        thread.setDaemon(true);
        return thread;
    });

    public ExpiringMap() {
        INSTANCES.add(new WeakReference<>(this));
        cleanup();
    }

    private static void cleanup() {
        INSTANCES.removeIf(ref -> ref.get() == null);
    }

    public static void expire(UUID player) {
        SCHEDULER.schedule(() -> removeNow(player), EXPIRY_DELAY.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static void removeNow(UUID player) {
        cleanup();
        for (WeakReference<ExpiringMap<?>> instance : new ArrayList<>(INSTANCES)) {
            ExpiringMap<?> map = instance.get();
            if (map == null) continue;
            map.remove(player);
        }
    }
}
