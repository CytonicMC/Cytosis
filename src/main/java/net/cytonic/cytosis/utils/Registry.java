/*
    This class was written by unjoinable, and the contributors to his MinigameLib. All credit goes to him.
    https://github.com/unjoinable/MinigameLib/blob/master/src/main/java/io/github/unjoinable/minigamelib/api/registry/Registry.java
 */
package net.cytonic.cytosis.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple registry superclass
 * @param <K> The registry key type.
 * @param <V> The registry value type.
 */
public abstract class Registry<K, V> implements Iterable<V> {
    private final Map<K, V> objects;

    /**
     * Creates the instance of registry
     */
    public Registry() {
        this.objects = new ConcurrentHashMap<>();
    }

    /**
     * Gets the object from registry and gives it, if the key was invalid it will throw an exception.
     *
     * @param key The key of the value wanted from the registry.
     * @return The value from the key provided.
     * @throws IllegalArgumentException In case if an invalid key is provided.
     */
    public V get(K key) {
        if (objects.containsKey(key)) {
            return objects.get(key);
        } else {
            throw new IllegalArgumentException("Object with key " + key + " does not exist in the registry");
        }
    }

    /**
     * @param key          The key to look for.
     * @param defaultValue Default value.
     * @return The value.
     */
    public V getOrDefault(K key, V defaultValue) {
        return objects.getOrDefault(key, defaultValue);
    }

    /**
     * It adds the provided key, value to the registry if the key already exists it throws an exception.
     *
     * @param key   The key of the value to be inserted into the registry.
     * @param value The value of the key to be inserted.
     * @throws IllegalArgumentException In case if the key already exists in the registry, or the key is null.
     */
    public void add(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (objects.containsKey(key)) {
            throw new IllegalArgumentException("Object already exists in the registry with key " + key);
        }
        objects.put(key, value);
    }

    /**
     * @param key The key which is to be removed from the registry.
     * @throws IllegalArgumentException When the object is not present in the registry this exception is thrown.
     */
    public void remove(K key) {
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
    public boolean contains(K key) {
        return objects.containsKey(key);
    }

    /**
     * @return Returns the map of all present objects in the registry
     */
    public Map<K, V> getAllObjects() {
        return objects;
    }

    @Override
    public @NotNull Iterator<V> iterator() {
        return objects.values().iterator();
    }
}