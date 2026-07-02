package net.cytonic.cytosis.registry;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

public abstract class Registry<V extends Keyed> implements Iterable<V> {

    @Getter
    protected final Key id;
    protected final Map<Key, V> objects;

    /**
     * Creates the instance of registry
     */
    protected Registry(Key id) {
        this.id = id;
        this.objects = new ConcurrentHashMap<>();
    }

    protected Registry(Key id, Map<Key, V> objects) {
        this.id = id;
        this.objects = objects;
    }

    /**
     * Gets the object from registry and gives it, if the key was invalid it will throw an exception.
     *
     * @param key The key of the value wanted from the registry.
     * @return The value from the key provided.
     * @throws IllegalArgumentException In case if an invalid key is provided.
     */
    public V get(Key key) {
        if (objects.containsKey(key)) {
            return objects.get(key);
        } else {
            throw new IllegalArgumentException("Object with key " + key + " does not exist in the registry");
        }
    }

    /**
     * It adds the provided key, value to the registry if the key already exists it throws an exception.
     *
     * @param key   The key of the value to be inserted into the registry.
     * @param value The value of the key to be inserted.
     * @throws IllegalArgumentException In case if the key already exists in the registry, or the key is null.
     */
    public void add(Key key, V value) {
        if (objects.containsKey(key)) {
            throw new IllegalArgumentException("Object already exists in the registry with key " + key);
        }
        objects.put(key, value);
    }

    public void add(V value) {
        add(value.key(), value);
    }

    /**
     * @param key The key which is to be removed from the registry.
     * @throws IllegalArgumentException When the object is not present in the registry this exception is thrown.
     */
    public void remove(Key key) {
        if (objects.containsKey(key)) {
            objects.remove(key);
        } else {
            throw new IllegalArgumentException("Object with key " + key + " does not exist in the registry");
        }
    }

    /**
     * @param key The object to check if it exists in the registry.
     * @return If the object is present or not.
     */
    public boolean contains(Key key) {
        return objects.containsKey(key);
    }

    /**
     * @return Returns the map of all present objects in the registry
     */
    public Map<Key, V> getMap() {
        return objects;
    }

    public Set<Key> keys() {
        return objects.keySet();
    }

    public Collection<V> values() {
        return objects.values();
    }

    public int size() {
        return objects.size();
    }

    public Collection<V> all() {
        return objects.values();
    }

    @Override
    public Iterator<V> iterator() {
        return objects.values().iterator();
    }
}
