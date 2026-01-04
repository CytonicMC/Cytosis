package net.cytonic.cytosis.managers;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.data.packet.packets.CooldownUpdatePacket;
import net.cytonic.cytosis.data.packet.packets.CooldownUpdatePacket.CooldownTarget;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.NatsManager;

/**
 * A class that handles network-wide cooldowns that sync across servers
 */
@CytosisComponent(dependsOn = {RedisDatabase.class, NatsManager.class, LocalCooldownManager.class})
public class NetworkCooldownManager implements Bootstrappable {

    private RedisDatabase redis;
    private NatsManager nats;
    private final Map<Key, Instant> global = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Key, Instant>> personal = new ConcurrentHashMap<>();

    public NetworkCooldownManager() {
    }

    @Override
    public void init() {
        this.redis = Cytosis.get(RedisDatabase.class);
        this.nats = Cytosis.get(NatsManager.class);
        importFromRedis();
    }

    /**
     * Converts a string to a personal key used to store hash sets in redis
     *
     * @param uuid the player in question
     * @return the string representing the key
     */
    public static String toPersonalKey(UUID uuid) {
        return "COOLDOWN_PERSONAL_KEY:" + uuid;
    }

    /**
     * Determines if the global cooldown is active
     *
     * @param id the {@link Key} id of the cooldown
     * @return if the specified cooldown is active
     */
    public boolean isOnGlobalCooldown(Key id) {
        if (!global.containsKey(id)) {
            return false;
        }
        Instant expire = global.get(id);
        if (expire.isAfter(Instant.now())) {
            return true;
        }
        global.remove(id);
        return false;
    }

    /**
     * Determines if a player is on cooldown for something
     *
     * @param uuid the player's uuid
     * @param id   the id of the cooldown
     * @return if the player is currently on cooldown for the specified cooldown
     */
    public boolean isOnPersonalCooldown(UUID uuid, Key id) {
        if (!personal.containsKey(uuid)) {
            return false;
        }
        if (!personal.get(uuid).containsKey(id)) {
            return false;
        }
        Instant expire = personal.get(uuid).get(id);
        if (expire.isAfter(Instant.now())) {
            return true;
        }
        personal.get(uuid).remove(id);
        return false;
    }

    /**
     * Resets the personal cooldown for the specified player and id
     *
     * @param uuid the {@link UUID} of the player
     * @param id   the {@link Key} id of the cooldown
     */
    public void resetPersonalCooldown(UUID uuid, Key id) {
        resetPersonalCooldown(uuid, id, true);
    }

    /**
     * Resets a player's individual cooldown (yay!)
     *
     * @param uuid   The player's uuid
     * @param id     the {@link Key} of the cooldown
     * @param notify if the server should notify other servers to update their cache
     */
    public void resetPersonalCooldown(UUID uuid, Key id, boolean notify) {
        if (!personal.containsKey(uuid)) return;
        personal.get(uuid).remove(id);
        redis.removeFromHash(toPersonalKey(uuid), id.asString());

        if (notify) {
            new CooldownUpdatePacket(CooldownTarget.PERSONAL, id, null, uuid).publish();
        }
    }

    /**
     * Resets the global cooldown for a specifiec cooldown
     *
     * @param id the {@link Key}  id of the cooldown
     */
    public void resetGlobalCooldown(Key id) {
        resetGlobalCooldown(id, true);
    }

    /**
     * Resets a global cooldown by the specified namespaced id
     *
     * @param id     the ID of the cooldown
     * @param notify if the server should notify other servers to update their local caches
     */
    public void resetGlobalCooldown(Key id, boolean notify) {
        if (!global.containsKey(id)) return;
        global.remove(id);
        if (notify) {
            new CooldownUpdatePacket(CooldownTarget.GLOBAL, id, null, null).publish();
        }
    }

    /**
     * Updates the global cooldown by the specified id
     *
     * @param id     The id of the cooldown
     * @param expire the instant it should expire at
     */
    public void setGlobal(Key id, @Nullable Instant expire) {
        setGlobal(id, expire, true);
    }

    /**
     * Updates the global cooldown by the specified id
     *
     * @param id     The id of the cooldown
     * @param expire the instant it should expire at
     */
    public void setGlobal(Key id, @Nullable Instant expire, boolean publish) {
        if (expire == null || expire.isBefore(Instant.now())) {
            resetGlobalCooldown(id, false);
            return;
        }
        global.put(id, expire);
        redis.addToHash(RedisDatabase.GLOBAL_COOLDOWNS_KEY, id.asString(), expire.toString());
        if (publish) {
            new CooldownUpdatePacket(CooldownTarget.GLOBAL, id, expire, null).publish();
        }
    }

    /**
     * Updates a player's cooldown by the specified UUID and Key
     *
     * @param uuid   The player
     * @param id     The id of the cooldown
     * @param expire the instant this cooldown should expire at
     */
    public void setPersonal(UUID uuid, Key id, Instant expire) {
        setPersonal(uuid, id, expire, true);
    }

    /**
     * Updates a player's cooldown by the specified UUID and Key
     *
     * @param uuid   The player
     * @param id     The id of the cooldown
     * @param expire the instant this cooldown should expire at
     */
    public void setPersonal(UUID uuid, Key id, Instant expire, boolean publish) {
        if (expire == null || expire.isBefore(Instant.now())) {
            resetPersonalCooldown(uuid, id, false);
            return;
        }
        if (!personal.containsKey(uuid)) {
            personal.put(uuid, new ConcurrentHashMap<>());
        }
        personal.get(uuid).put(id, expire);
        redis.addToHash(toPersonalKey(uuid), id.asString(), expire.toString());
        if (publish) {
            new CooldownUpdatePacket(CooldownTarget.PERSONAL, id, expire, uuid).publish();
        }
    }

    /**
     * Imports the current cooldowns from redis
     */
    public void importFromRedis() {
        long start = System.currentTimeMillis();
        Set<String> personalKeys = redis.getKeys("COOLDOWN_PERSONAL_KEY:*");
        for (String personalKey : personalKeys) {
            UUID uuid = UUID.fromString(personalKey.split("COOLDOWN_PERSONAL_KEY:")[1]);

            Map<String, String> results = redis.getHash(personalKey);
            Map<Key, Instant> personalMap = new HashMap<>();

            results.forEach((key, value) -> personalMap.put(Key.key(key), Instant.parse(value)));
            personal.put(uuid, personalMap);
        }
        long end = System.currentTimeMillis();
        Logger.info("Loaded " + personalKeys.size() + " players' cooldowns in " + (end - start) + " ms!");

        start = System.currentTimeMillis();
        Map<String, String> results = redis.getHash(RedisDatabase.GLOBAL_COOLDOWNS_KEY);
        Map<Key, Instant> globalMap = new HashMap<>();

        results.forEach((key, value) -> globalMap.put(Key.key(key), Instant.parse(value)));
        global.putAll(globalMap);
        end = System.currentTimeMillis();

        Logger.info("Loaded " + globalMap.size() + " global cooldowns in " + (end - start) + " ms!");
    }

    /**
     * Returns the expiry of a specific global cooldown
     *
     * @param node the cooldown id
     * @return the instant it expires.
     */
    @Nullable
    public Instant getGlobalExpiry(Key node) {
        return global.get(node);
    }

    /**
     * Returns the expiry of a specific personal cooldown
     *
     * @param node the cooldown id
     * @return the instant it expires.
     */
    @Nullable
    public Instant getPersonalExpiry(UUID uuid, Key node) {
        return personal.get(uuid).get(node);
    }

    /**
     * Gets the keys of the currently active global cooldowns
     *
     * @return the set of global keys
     */
    public Set<Key> getGlobalKeys() {
        return global.keySet();
    }

    /**
     * Gets every key currently on cooldown, both global and personal.
     *
     * @return The set of every tracked key
     */
    public Set<Key> getAllKeys() {
        Set<Key> keys = getPersonalKeys();
        keys.addAll(global.keySet());
        return keys;
    }

    /**
     * Gets the keys of the currently active personal cooldowns (every player)
     *
     * @return the set of personal keys
     */
    public Set<Key> getPersonalKeys() {
        Set<Key> personalKeys = new HashSet<>();
        personal.forEach((uuid, map) -> map.forEach((key, instant) -> personalKeys.add(key)));
        return personalKeys;
    }
}