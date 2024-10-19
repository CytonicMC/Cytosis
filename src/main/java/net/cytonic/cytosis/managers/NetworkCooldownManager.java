package net.cytonic.cytosis.managers;

import jakarta.annotation.Nullable;
import net.cytonic.containers.CooldownUpdateContainer;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.logging.Logger;
import net.minestom.server.utils.NamespaceID;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that handles network-wide cooldowns that sync across servers
 */
public class NetworkCooldownManager {
    private final RedisDatabase redis;
    private final Map<NamespaceID, Instant> global = new ConcurrentHashMap<>();
    private final Map<UUID, Map<NamespaceID, Instant>> personal = new ConcurrentHashMap<>();

    /**
     * A default constructor
     */
    public NetworkCooldownManager(RedisDatabase redis) {
        this.redis = redis;
    }

    /**
     * Converts a string to a personal key used to store hash sets in redis
     *
     * @param uuid the player in question
     * @return the string representing the key
     */
    @SuppressWarnings("preview")
    public static String toPersonalKey(UUID uuid) {
        return STR."COOLDOWN_PERSONAL_KEY:\{uuid}";
    }

    /**
     * Determines if the global cooldown is active
     *
     * @param id the {@link NamespaceID} id of the cooldown
     * @return if the specified cooldown is active
     */
    public boolean isOnGlobalCooldown(NamespaceID id) {
        if (!global.containsKey(id)) return false;
        Instant expire = global.get(id);
        if (expire.isAfter(Instant.now())) return true;
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
    public boolean isOnPersonalCooldown(UUID uuid, NamespaceID id) {
        if (!personal.containsKey(uuid)) return false;
        if (!personal.get(uuid).containsKey(id)) return false;
        Instant expire = personal.get(uuid).get(id);
        if (expire.isAfter(Instant.now())) return true;
        personal.get(uuid).remove(id);
        return false;
    }

    /**
     * Resets the personal cooldown for the specified player and id
     *
     * @param uuid the {@link UUID} of the player
     * @param id   the {@link NamespaceID} id of the cooldown
     */
    public void resetPersonalCooldown(UUID uuid, NamespaceID id) {
        resetPersonalCooldown(uuid, id, true);
    }

    /**
     * Resets the global cooldown for a specifiec cooldown
     *
     * @param id the {@link NamespaceID}  id of the cooldown
     */
    public void resetGlobalCooldown(NamespaceID id) {
        resetGlobalCooldown(id, true);
    }

    /**
     * Resets a player's individual cooldown (yay!)
     *
     * @param uuid   The player's uuid
     * @param id     the {@link NamespaceID} of the cooldown
     * @param notify if the server should notify other servers to update their cache
     */
    public void resetPersonalCooldown(UUID uuid, NamespaceID id, boolean notify) {
        if (!personal.containsKey(uuid)) return;
        personal.get(uuid).remove(id);
        redis.removeFromHash(toPersonalKey(uuid), id.asString());

        if (notify) {
            CooldownUpdateContainer container = new CooldownUpdateContainer(CooldownUpdateContainer.CooldownTarget.PERSONAL, id, null, uuid);
            redis.publish(RedisDatabase.COOLDOWN_UPDATE_CHANNEL, container.serialize());
        }
    }

    /**
     * Resets a global cooldown by the specified namespaced id
     *
     * @param id     the ID of the cooldown
     * @param notify if the server should notify other servers to update their local caches
     */
    public void resetGlobalCooldown(NamespaceID id, boolean notify) {
        if (!global.containsKey(id)) return;
        global.remove(id);
        if (notify) {
            CooldownUpdateContainer container = new CooldownUpdateContainer(CooldownUpdateContainer.CooldownTarget.PERSONAL, id, null, null);
            redis.publish(RedisDatabase.COOLDOWN_UPDATE_CHANNEL, container.serialize());
        }
    }

    /**
     * Updates the global cooldown by the specified id
     *
     * @param id     The id of the cooldown
     * @param expire the instant it should expire at
     */
    public void setGlobal(NamespaceID id, @Nullable Instant expire) {
        if (expire == null || expire.isBefore(Instant.now())) {
            resetGlobalCooldown(id, false);
            return;
        }
        global.put(id, expire);
        redis.addToHash(RedisDatabase.GLOBAL_COOLDOWNS_KEY, id.asString(), expire.toString());
        CooldownUpdateContainer container = new CooldownUpdateContainer(CooldownUpdateContainer.CooldownTarget.GLOBAL, id, expire, null);
        redis.publish(RedisDatabase.COOLDOWN_UPDATE_CHANNEL, container.serialize());
    }

    /**
     * Updates a player's cooldown by the specified UUID and NamespaceID
     *
     * @param uuid   The player
     * @param id     The id of the cooldown
     * @param expire the instant this cooldown should expire at
     */
    public void setPersonal(UUID uuid, NamespaceID id, Instant expire) {
        if (expire == null || expire.isBefore(Instant.now())) {
            resetPersonalCooldown(uuid, id, false);
            return;
        }
        if (!personal.containsKey(uuid)) personal.put(uuid, new ConcurrentHashMap<>());
        personal.get(uuid).put(id, expire);
        redis.addToHash(toPersonalKey(uuid), id.asString(), expire.toString());
        CooldownUpdateContainer container = new CooldownUpdateContainer(CooldownUpdateContainer.CooldownTarget.PERSONAL, id, expire, uuid);
        redis.publish(RedisDatabase.COOLDOWN_UPDATE_CHANNEL, container.serialize());
    }

    /**
     * Imports the current cooldowns from redis
     */
    @SuppressWarnings("preview")
    public void importFromRedis() {
        long start = System.currentTimeMillis();
        Set<String> personalKeys = redis.getKeys("COOLDOWN_PERSONAL_KEY:*");
        for (String personalKey : personalKeys) {
            UUID uuid = UUID.fromString(personalKey.split("COOLDOWN_PERSONAL_KEY:")[1]);

            Map<String, String> results = redis.getHash(personalKey);
            Map<NamespaceID, Instant> personalMap = new HashMap<>();

            results.forEach((key, value) -> personalMap.put(NamespaceID.from(key), Instant.parse(value)));
            personal.put(uuid, personalMap);
        }
        long end = System.currentTimeMillis();
        Logger.info(STR."Loaded \{personalKeys.size()} players' cooldowns in \{end - start} ms!");

        start = System.currentTimeMillis();
        Map<String, String> results = redis.getHash(RedisDatabase.GLOBAL_COOLDOWNS_KEY);
        Map<NamespaceID, Instant> globalMap = new HashMap<>();

        results.forEach((key, value) -> globalMap.put(NamespaceID.from(key), Instant.parse(value)));
        global.putAll(globalMap);
        end = System.currentTimeMillis();

        Logger.info(STR."Loaded \{globalMap.size()} global cooldowns in \{end - start} ms!");
    }

    /**
     * Returns the expiry of a specific global cooldown
     *
     * @param node the cooldown id
     * @return the instant it expires.
     */
    @Nullable
    public Instant getGlobalExpiry(NamespaceID node) {
        return global.get(node);
    }

    /**
     * Returns the expiry of a specific personal cooldown
     *
     * @param node the cooldown id
     * @return the instant it expires.
     */
    @Nullable
    public Instant getPersonalExpiry(UUID uuid, NamespaceID node) {
        return personal.get(uuid).get(node);
    }

    /**
     * Gets the keys of the currently active personal cooldowns (every player)
     *
     * @return the set of personal keys
     */
    public Set<NamespaceID> getPersonalKeys() {
        Set<NamespaceID> personalKeys = new HashSet<>();
        personal.forEach((_, namespaceIDInstantMap) -> namespaceIDInstantMap.forEach((namespaceID, _) -> personalKeys.add(NamespaceID.from(namespaceID))));
        return personalKeys;
    }

    /**
     * Gets the keys of the currently active global cooldowns
     *
     * @return the set of global keys
     */
    public Set<NamespaceID> getGlobalKeys() {
        return global.keySet();
    }

    /**
     * Gets every key currently on cooldown, both global and personal.
     *
     * @return The set of every tracked key
     */
    public Set<NamespaceID> getAllKeys() {
        Set<NamespaceID> keys = getPersonalKeys();
        keys.addAll(global.keySet());
        return keys;
    }
}
