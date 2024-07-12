package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.data.enums.CytosisPreferences;
import net.cytonic.cytosis.data.objects.PreferenceData;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.objects.NamespacedPreference;
import net.minestom.server.utils.NamespaceID;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.cytonic.cytosis.data.DatabaseTemplate.QUERY;
import static net.cytonic.cytosis.data.DatabaseTemplate.UPDATE;

/**
 * A manager class holding perference data for users. An example is if they are accepting friend requests.
 * Since preferences are pretty small, every online player has their preference data stored here, no matter
 * which server they are connected to.
 */
public class PreferenceManager {

    /**
     * The registry of preferences currently registered. The held value of the {@link NamespacedPreference} is
     * the default value passed if none is found in the player's preferences
     */
    public static final Map<NamespaceID, NamespacedPreference<?>> PREFERENCE_REGISTRY = new ConcurrentHashMap<>();
    private final Map<UUID, PreferenceData> preferenceData = new ConcurrentHashMap<>();

    /**
     * Default constructor
     */
    public PreferenceManager() {
        PREFERENCE_REGISTRY.put(NamespaceID.from("cytosis:accept_friend_request"), CytosisPreferences.ACCEPT_FRIEND_REQUESTS);
        UPDATE."CREATE TABLE IF NOT EXISTS cytonic_preferences (uuid VARCHAR(36), preferences TEXT)".whenComplete((_, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst creating the preferences table!", throwable);
        });
    }

    /**
     * Loads the player's preferences into memory from the database
     *
     * @param uuid the player
     */
    public void loadPlayerPreferences(UUID uuid) {
        QUERY."SELECT preferences FROM cytonic_preferences WHERE uuid = '\{uuid.toString()}';".whenComplete((rs, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst loading preferences!", throwable);
                return;
            }

            try {
                if (rs.next()) {
                    PreferenceData data;
                    try {
                        data = PreferenceData.deserialize(rs.getString("preferences"));
                    } catch (Exception e) {
                        Logger.error("An error occurred whilst loading preferences!", e);
                        return;
                    }
                    preferenceData.put(uuid, data);
                }
            } catch (SQLException e) {
                Logger.error("An error occurred whilst loading preferences!", e);
            }
        });
    }

    /**
     * Writes the players preferences to the database and removes them from memory
     *
     * @param uuid the player
     */
    public void unloadPlayerPreferences(UUID uuid) {
        UPDATE."UPDATE cytonic_preferences SET preferences = '\{preferenceData.get(uuid).serialize()}' WHERE uuid = '\{uuid.toString()}';".whenComplete((_, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst updating preferences!", throwable);
        });
        preferenceData.remove(uuid);
    }


    /**
     * Update a player's preference data
     *
     * @param uuid        the player to update
     * @param namespaceID the id of the preference
     * @param value       the value to set
     * @throws IllegalStateException    if the player has no preference data
     * @throws IllegalArgumentException if the value is of the incorrect type
     */
    public void updatePlayerPreference(UUID uuid, NamespaceID namespaceID, Object value) {
        NamespacedPreference<?> preference = PREFERENCE_REGISTRY.get(namespaceID);
        if (!preferenceData.containsKey(uuid)) {
            PreferenceData data = new PreferenceData(new ConcurrentHashMap<>());
            data.set(namespaceID, value);
            preferenceData.put(uuid, data);

            UPDATE."INSERT INTO cytonic_preferences VALUES('\{uuid.toString()}', '\{data.serialize()}');".whenComplete((_, throwable) -> {
                if (throwable != null) Logger.error("An error occurred whilst updating preferences!", throwable);
            });
            return;
        }

        if (preference.value().getClass() != value.getClass())
            throw new IllegalArgumentException(STR."Cannot set a preference of type \{value.getClass().getSimpleName()} with a preference of type \{preference.value().getClass().getSimpleName()}");
        preferenceData.get(uuid).set(namespaceID, value);
        UPDATE."UPDATE cytonic_preferences SET preferences = '\{preferenceData.get(uuid).serialize()}' WHERE uuid = '\{uuid.toString()}';".whenComplete((_, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst updating preferences!", throwable);
        });
    }

    /**
     * Gets the preference of a player
     *
     * @param uuid        The player
     * @param namespaceID the namespace
     * @param <T>         the type of the preference
     * @return the player's preference
     */
    public <T> T getPlayerPreference(UUID uuid, NamespaceID namespaceID) {
        if (!PREFERENCE_REGISTRY.containsKey(namespaceID))
            throw new IllegalArgumentException(STR."The preference \{namespaceID} does not exist!");
        if (!preferenceData.containsKey(uuid)) return (T) PREFERENCE_REGISTRY.get(namespaceID).value();
        return preferenceData.get(uuid).get(namespaceID);
    }

    /**
     * Gets the preference of a player
     *
     * @param uuid        The player
     * @param namespaceID the namespace
     * @param <T>         the type of the preference
     * @return the player's preference
     */
    public <T> T getPlayerPreference(UUID uuid, NamespacedPreference<T> namespaceID) {
        if (!PREFERENCE_REGISTRY.containsKey(namespaceID.namespaceID()))
            throw new IllegalArgumentException(STR."The preference \{namespaceID.namespaceID()} does not exist!");
        if (!preferenceData.containsKey(uuid)) return (T) PREFERENCE_REGISTRY.get(namespaceID.namespaceID()).value();
        return preferenceData.get(uuid).get(namespaceID.namespaceID());
    }

    /**
     * Returns the registry of preferences
     *
     * @return the registry of preferences, keyed by namespace
     */
    public Map<NamespaceID, NamespacedPreference<?>> getPreferenceRegistry() {
        return PREFERENCE_REGISTRY;
    }
}
