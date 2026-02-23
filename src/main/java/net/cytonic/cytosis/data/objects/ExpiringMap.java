package net.cytonic.cytosis.data.objects;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExpiringMap<V> extends ConcurrentHashMap<UUID, V> {

    // we use a weak reference to ensure that the maps won't be prevented from being GC'd
    private static final Queue<WeakReference<ExpiringMap<?>>> INSTANCES = new ConcurrentLinkedQueue<>();

    public ExpiringMap() {
        INSTANCES.add(new WeakReference<>(this));
        cleanup();
    }

    private static void cleanup() {
        INSTANCES.removeIf(ref -> ref.get() == null);
    }

    public static void expire(UUID player) {
        cleanup();
        for (WeakReference<ExpiringMap<?>> instance : new ArrayList<>(INSTANCES)) {
            ExpiringMap<?> map = instance.get();
            assert map != null;
            map.remove(player);
        }
    }
}
