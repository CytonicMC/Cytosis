package net.cytonic.cytosis.managers;

import lombok.SneakyThrows;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.NamespacedPreference;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.objects.preferences.PreferenceData;
import net.cytonic.cytosis.data.objects.preferences.PreferenceRegistry;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
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
@SuppressWarnings("preview")
public class PreferenceManager {

    /**
     * The registry of preferences currently registered. The held preference of the {@link NamespacedPreference} is
     * the default preference passed if none is found in the player's preferences
     */
    public static final PreferenceRegistry PREFERENCE_REGISTRY = new PreferenceRegistry();
    private final Map<UUID, PreferenceData> preferenceData = new ConcurrentHashMap<>();

    /**
     * Default constructor
     */
    public PreferenceManager() {

        PREFERENCE_REGISTRY.write(CytosisNamespaces.ACCEPT_FRIEND_REQUESTS, CytosisPreferences.ACCEPT_FRIEND_REQUESTS);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.SERVER_ALERTS, CytosisPreferences.SERVER_ALERTS);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.CHAT_CHANNEL, CytosisPreferences.CHAT_CHANNEL);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.VANISHED, CytosisPreferences.VANISHED);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.IGNORED_CHAT_CHANNELS, CytosisPreferences.IGNORED_CHAT_CHANNELS);

        UPDATE."CREATE TABLE IF NOT EXISTS cytonic_preferences (uuid VARCHAR(36) PRIMARY KEY, preferences TEXT)".whenComplete((_, throwable) -> {
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
        if (!preferenceData.containsKey(uuid)) {
            return;
        }
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
     * @param value       the preference to set
     * @param <T>         the type of the preference
     * @throws IllegalStateException    if the player has no preference data
     * @throws IllegalArgumentException if the preference is of the incorrect type
     */
    public <T> void updatePlayerPreference(UUID uuid, TypedNamespace<T> namespaceID, T value) {
        PreferenceRegistry.Entry<T> entry = PREFERENCE_REGISTRY.get(namespaceID);
        Preference<T> preference = entry.preference();

        if (preference.value() != null && preference.value().getClass() != value.getClass())
            throw new IllegalArgumentException(STR."Cannot set a preference of type \{value.getClass().getSimpleName()} with a preference of type \{preference.value().getClass().getSimpleName()}");


        if (!preferenceData.containsKey(uuid)) {
            PreferenceData data = new PreferenceData(new ConcurrentHashMap<>());
            data.set(namespaceID, value);
            preferenceData.put(uuid, data);
            addNewPlayerPreference(uuid, data);
            return;
        }

        preferenceData.get(uuid).set(namespaceID, value);
        persistPreferences(uuid, preferenceData.get(uuid));
    }

    @SneakyThrows
    public <T> void updatePlayerPreference(UUID uuid, NamespacedPreference<T> pref, T val) {
        NamespacedPreference<T> preference = pref.clone(); // as to not mutate it....
        preference.value(val);
        if (!preferenceData.containsKey(uuid)) {
            PreferenceData data = new PreferenceData(new ConcurrentHashMap<>());
            data.set(preference);
            preferenceData.put(uuid, data);
            addNewPlayerPreference(uuid, data);
            return;
        }
        preferenceData.get(uuid).set(preference);
        persistPreferences(uuid, preferenceData.get(uuid));
    }

    /**
     * Update a player's preference data
     *
     * @param uuid       the player to update
     * @param preference the namespace
     * @param value      the preference to set
     * @param <T>        the type of the preference
     * @throws IllegalStateException    if the player has no preference data
     * @throws IllegalArgumentException if the preference is of the incorrect type
     * @throws ClassCastException       if the preference is of the incorrect type
     */
    @SuppressWarnings("unchecked") // it is a checked cast
    public <T> void updatePlayerPreference_UNSAFE(UUID uuid, NamespaceID preference, @Nullable T value) {
        TypedNamespace<?> typed = PREFERENCE_REGISTRY.typedNamespaces().stream().filter(t -> t.namespaceID().equals(preference)).findFirst().orElse(null);
        if (typed == null) throw new IllegalArgumentException(STR."The preference \{preference} does not exist!");
        if (value != null && typed.type() != value.getClass())
            throw new IllegalArgumentException(STR."Cannot set a preference \{preference.asString()} of type \{value.getClass().getSimpleName()} with a preference of type \{typed.type().getSimpleName()}");
        updatePlayerPreference(uuid, (TypedNamespace<T>) typed, value);
    }

    /**
     * Gets the preference of a player
     *
     * @param uuid        The player
     * @param namespaceID the namespace
     * @param <T>         the type of the preference
     * @return the player's preference
     */
    @SuppressWarnings("unchecked")
    public <T> T getPlayerPreference_UNSAFE(UUID uuid, NamespaceID namespaceID) {
        TypedNamespace<?> typed = PREFERENCE_REGISTRY.typedNamespaces().stream().filter(t -> t.namespaceID().equals(namespaceID)).findFirst().orElse(null);
        if (typed == null) throw new IllegalArgumentException(STR."The preference \{namespaceID} does not exist!");
        return getPlayerPreference(uuid, (TypedNamespace<T>) typed);
    }

    /**
     * Gets the preference of a player
     *
     * @param uuid        The player
     * @param namespaceID the namespace
     * @param <T>         the type of the preference
     * @return the player's preference
     */
    public <T> T getPlayerPreference(UUID uuid, TypedNamespace<T> namespaceID) {
        if (!PREFERENCE_REGISTRY.contains(namespaceID))
            throw new IllegalArgumentException(STR."The preference \{namespaceID} is not in the registry!");
        if (!preferenceData.containsKey(uuid)) return PREFERENCE_REGISTRY.get(namespaceID).preference().value();
        return preferenceData.get(uuid).get(namespaceID);
    }

    /**
     * Gets the preference of a player
     *
     * @param uuid       The player
     * @param preference the namespace
     * @param <T>        the type of the preference
     * @return the player's preference
     */
    public <T> T getPlayerPreference(UUID uuid, NamespacedPreference<T> preference) {
        if (!PREFERENCE_REGISTRY.contains(preference.typedNamespace()))
            throw new IllegalArgumentException(STR."The preference \{preference.namespace().asString()} does not exist!");
        if (!preferenceData.containsKey(uuid))
            return PREFERENCE_REGISTRY.get(preference.typedNamespace()).preference().value();
        return preferenceData.get(uuid).get(preference);
    }

    /**
     * Returns the registry of preferences
     *
     * @return the registry of preferences, keyed by namespace
     */
    public PreferenceRegistry getPreferenceRegistry() {
        return PREFERENCE_REGISTRY;
    }

    @SneakyThrows
    public void persistPreferences(UUID uuid, PreferenceData preferenceData) {
        MysqlDatabase db = Cytosis.getDatabaseManager().getMysqlDatabase();
        PreparedStatement ps = db.prepareStatement("UPDATE cytonic_preferences SET preferences = ? WHERE uuid = ?;");
        ps.setString(2, uuid.toString());
        ps.setString(1, preferenceData.serialize());

        db.update(ps).whenComplete((_, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst updating preferences!", throwable);
        });
    }

    @SneakyThrows
    public void addNewPlayerPreference(UUID uuid, PreferenceData data) {

        MysqlDatabase db = Cytosis.getDatabaseManager().getMysqlDatabase();
        PreparedStatement ps = db.prepareStatement("INSERT INTO cytonic_preferences VALUES(?,?);");
        ps.setString(1, uuid.toString());
        ps.setString(2, data.serialize());

        db.update(ps).whenComplete((_, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst updating preferences!", throwable);
        });
    }
}
