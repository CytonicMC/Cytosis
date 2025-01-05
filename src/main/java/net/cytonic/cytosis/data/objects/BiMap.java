package net.cytonic.cytosis.data.objects;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * A class that holds a bidirectional map, meaning it can be queried by key AND value. Under the hood, it uses a ConcurrentHashMap, making it thread safe.
 *
 * @param <K> The key type
 * @param <V> The value type
 */
@SuppressWarnings("unused")
public class BiMap<K, V> implements Iterable<Map.Entry<K, V>> {

    private final ConcurrentHashMap<K, V> keyToValue = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<V, K> valueToKey = new ConcurrentHashMap<>();
    /**
     * A default constructor
     */
    public BiMap() {
        // do nothing
    }

    /**
     * Add a key and value to the map
     *
     * @param key   The key
     * @param value The value
     */
    public void put(K key, V value) {
        keyToValue.put(key, value);
        valueToKey.put(value, key);
    }

    /**
     * Remove a key and value from the map
     *
     * @param key   The key
     * @param value The value
     */
    public void remove(K key, V value) {
        keyToValue.remove(key);
        valueToKey.remove(value);
    }

    /**
     * Removes all keys and values from the map
     */
    public void clear() {
        keyToValue.clear();
        valueToKey.clear();
    }

    /**
     * Gets the number of entries in the map
     *
     * @return the number of entries
     */
    public int size() {
        return keyToValue.size();
    }

    /**
     * Gets the value associated with the key
     *
     * @param key The key to query by
     * @return The value associated with the key
     */
    public V getByKey(K key) {
        return keyToValue.get(key);
    }

    /**
     * Gets the key associated with the value
     *
     * @param value the value to query by
     * @return the key associated with the value
     */
    public K getByValue(V value) {
        return valueToKey.get(value);
    }

    /**
     * Tests if the key is in the map
     *
     * @param key the key
     * @return if the key is in the map
     */
    public boolean containsKey(K key) {
        return keyToValue.containsKey(key);
    }

    /**
     * tests if the value is in the map
     *
     * @param value the value
     * @return if the value is in the map
     */
    public boolean containsValue(V value) {
        return valueToKey.containsKey(value);
    }

    /**
     * Iterates through the map
     *
     * @return the iterator
     */
    @NotNull
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return keyToValue.entrySet().iterator();
    }

    /**
     * A foreach loop for the map
     *
     * @param action the consumer
     */
    public void forEach(BiConsumer<K, V> action) {
        keyToValue.forEach(action);
    }

    /**
     * The keys for the map
     *
     * @return the keys
     */
    public Set<K> getKeys() {
        return keyToValue.keySet();
    }

    /**
     * The value set for the map
     *
     * @return the values
     */
    public Set<V> getValues() {
        return valueToKey.keySet();
    }
}
