package net.cytonic.cytosis.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.EnvironmentDatabase;
import net.cytonic.cytosis.data.GlobalDatabase;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.objects.preferences.PreferenceData;
import net.cytonic.cytosis.data.objects.preferences.PreferenceRegistry;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

/**
 * A manager class holding preference data for users. An example is if they are accepting friend requests. Since
 * preferences are pretty small, every online player has their preference data stored here, no matter which server they
 * are connected to.
 */
@CytosisComponent(dependsOn = {EnvironmentDatabase.class})
public class PreferenceManager implements Bootstrappable {

    /**
     * The registry of preferences currently registered. The held preference of the {@link Preference} is the default
     * preference passed if none is found in the player's preferences
     */
    public static final PreferenceRegistry PREFERENCE_REGISTRY = new PreferenceRegistry();
    private final Map<UUID, PreferenceData> preferenceData = new ConcurrentHashMap<>();
    private GlobalDatabase db;

    /**
     * Default constructor
     */
    public PreferenceManager() {
    }

    @Override
    public void init() {
        this.db = Cytosis.get(GlobalDatabase.class);
        Preferences.ALL.forEach(PREFERENCE_REGISTRY::write);
    }

    /**
     * Loads the player's preferences into memory from the database
     *
     * @param uuid the player
     */
    public void loadPlayerPreferences(UUID uuid) {
        db.loadPlayerPreferences(uuid).thenAccept(data -> {
            if (data == null) {
                data = new PreferenceData(new ArrayList<>());
                Logger.debug("Needs to be created!");
                db.addNewPlayerPreferences(uuid, data);
            }
            preferenceData.put(uuid, data);
            data.get(Preferences.LISTENING_SNOOPS).snoops().forEach(s -> {
                if (Cytosis.get(SnooperManager.class).getChannel(Key.key(s)) == null) {
                    // big problem if null
                    Logger.warn(
                        "Player " + uuid + " is listening to the channel '" + s + "', but it isn't registered!");
                    Cytosis.getPlayer(uuid).ifPresent(player -> player.sendMessage(Msg.mm(
                        "<red><b>ERROR!</b></red><gray> Failed to start listening on snooper channel '" + s
                            + "'")));
                }
            });
        }).exceptionally(throwable -> {
            Logger.error("Failed to load player preferences", throwable);
            return null;
        });
    }

    /**
     * Writes the players preferences to the database and removes them from memory
     *
     * @param uuid the player
     */
    public void unloadPlayerPreferences(UUID uuid) {
        if (!preferenceData.containsKey(uuid)) return;
        db.persistPlayerPreferences(uuid, preferenceData.get(uuid));
        preferenceData.remove(uuid);
    }


    /**
     * Update a player's preference data
     *
     * @param uuid        the player to update
     * @param key the id of the preference
     * @param value       the preference to set
     * @param <T>         the type of the preference
     * @throws IllegalStateException    if the player has no preference data
     * @throws IllegalArgumentException if the preference is of the incorrect type
     */
    @SuppressWarnings("unchecked")
    public <T> void updatePlayerPreference_UNSAFE(UUID uuid, Key key, @Nullable T value) {
        Preference<?> entry = PREFERENCE_REGISTRY.get(key);

        if (value != null && !entry.getType().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(
                "Cannot set a preference of type " + value.getClass().getSimpleName() + " with a preference of type "
                    + entry.getType().getSimpleName());
        }

        Preference<T> safe = (Preference<T>) entry;
        safe = safe.withValue(value);
        if (!preferenceData.containsKey(uuid)) {
            PreferenceData data = new PreferenceData(new ArrayList<>());
            data.get(safe);
            preferenceData.put(uuid, data);
            db.addNewPlayerPreferences(uuid, data);
            return;
        }

        preferenceData.get(uuid).set(safe);
        db.persistPlayerPreferences(uuid, preferenceData.get(uuid));
    }

    @SneakyThrows
    public <T> void updatePlayerPreference(UUID uuid, Preference<T> pref, T val) {
        Preference<T> preference = pref.withValue(val);

        if (!preferenceData.containsKey(uuid)) {
            PreferenceData data = new PreferenceData(new ArrayList<>());
            data.set(preference);
            preferenceData.put(uuid, data);
            db.addNewPlayerPreferences(uuid, data);
            return;
        }
        preferenceData.get(uuid).set(preference);
        db.persistPlayerPreferences(uuid, preferenceData.get(uuid));
    }

    /**
     * Gets the preference of a player, with a few more ways of throwing an error.
     *
     * @param uuid      The player
     * @param key the namespace
     * @param <T>       the type of the preference
     * @return the player's preference
     */
    @SuppressWarnings("unchecked")
    public <T> T getPlayerPreference_UNSAFE(UUID uuid, Key key) {
        Preference<T> pref = (Preference<T>) PREFERENCE_REGISTRY.get(key);
        if (pref == null) {
            // this preference isn't supplied by this server.
            if (!preferenceData.containsKey(uuid)) return null;
            return preferenceData.get(uuid).getOr(key, null);
        }
        return getPlayerPreference(uuid, pref);
    }

    @Nullable
    public Preference<?> getPlayerPreferenceRaw(UUID uuid, Key key) {
        if (!preferenceData.containsKey(uuid)) return null;
        Preference<?> pref = preferenceData.get(uuid).getPreference(key);
        if (pref != null) return pref;
        return PREFERENCE_REGISTRY.get(key);
    }

    public Set<Key> getPlayerKeys(UUID uuid) {
        return preferenceData.getOrDefault(uuid, new PreferenceData(new ArrayList<>()))
            .keys();
    }


    /**
     * Gets the preference of a player
     *
     * @param uuid       The player
     * @param preference the namespace
     * @param <T>        the type of the preference
     * @return the player's preference
     */
    public <T> T getPlayerPreference(UUID uuid, Preference<T> preference) {
        if (!PREFERENCE_REGISTRY.contains(preference)) {
            throw new IllegalArgumentException(
                "The preference " + preference.getKey() + " has not been registered!");
        }
        if (!preferenceData.containsKey(uuid)) {
            return preference.getValue();
        }
        return preferenceData.get(uuid).get(preference);
    }

    private boolean isValid(UUID uuid, Key key) {
        Set<Key> valid = new HashSet<>();
        if (preferenceData.containsKey(uuid)) {
            valid.addAll(preferenceData.get(uuid).keys());
        }
        valid.addAll(PREFERENCE_REGISTRY.keys());
        return valid.contains(key);
    }

    /**
     * Returns the registry of preferences
     *
     * @return the registry of preferences, keyed by namespace
     */
    public PreferenceRegistry getPreferenceRegistry() {
        return PREFERENCE_REGISTRY;
    }
}
