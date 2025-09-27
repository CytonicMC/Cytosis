package net.cytonic.cytosis.managers;

import lombok.SneakyThrows;
import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.data.DatabaseManager;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.NamespacedPreference;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.objects.preferences.PreferenceData;
import net.cytonic.cytosis.data.objects.preferences.PreferenceRegistry;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.CytosisNamespaces;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager class holding preference data for users. An example is if they are accepting friend requests.
 * Since preferences are pretty small, every online player has their preference data stored here, no matter
 * which server they are connected to.
 */
@CytosisComponent(dependsOn = {DatabaseManager.class})
public class PreferenceManager implements Bootstrappable {
    private MysqlDatabase db;

    /**
     * The registry of preferences currently registered. The held preference of the {@link NamespacedPreference} is
     * the default preference passed if none is found in the player's preferences
     */
    public static final PreferenceRegistry PREFERENCE_REGISTRY = new PreferenceRegistry();
    private final Map<UUID, PreferenceData> preferenceData = new ConcurrentHashMap<>();

    @Override
    public void init() {
        this.db = Cytosis.CONTEXT.getComponent(DatabaseManager.class).getMysqlDatabase();

        PREFERENCE_REGISTRY.write(CytosisNamespaces.ACCEPT_FRIEND_REQUESTS, CytosisPreferences.ACCEPT_FRIEND_REQUESTS);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.SERVER_ALERTS, CytosisPreferences.SERVER_ALERTS);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.CHAT_CHANNEL, CytosisPreferences.CHAT_CHANNEL);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.VANISHED, CytosisPreferences.VANISHED);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.LOBBY_PLAYER_VISIBILITY, CytosisPreferences.LOBBY_PLAYER_VISIBILITY);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.IGNORED_CHAT_CHANNELS, CytosisPreferences.IGNORED_CHAT_CHANNELS);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.LISTENING_SNOOPS, CytosisPreferences.LISTENING_SNOOPS);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.MUTE_SNOOPER, CytosisPreferences.MUTE_SNOOPER);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.NICKNAME_DATA, CytosisPreferences.NICKNAME_DATA);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.NICKED_UUID, CytosisPreferences.NICKED_UUID);
        PREFERENCE_REGISTRY.write(CytosisNamespaces.CHAT_MESSAGE_PING, CytosisPreferences.CHAT_MESSAGE_PING);

        PreparedStatement ps = db.prepare("CREATE TABLE IF NOT EXISTS cytonic_preferences (uuid VARCHAR(36) PRIMARY KEY, preferences TEXT)");
        db.update(ps).whenComplete((unused, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst creating the preferences table!", throwable);
        });
    }

    /**
     * Default constructor
     */
    public PreferenceManager() {
    }

    /**
     * Loads the player's preferences into memory from the database
     *
     * @param uuid the player
     */
    public void loadPlayerPreferences(UUID uuid) {
        PreparedStatement load = db.prepare("SELECT * FROM cytonic_preferences WHERE uuid = ?");
        try {
            load.setString(1, uuid.toString());
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to load player preference!", exception);
        }
        db.query(load).whenComplete((rs, throwable) -> {
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
                    data.get(CytosisPreferences.LISTENING_SNOOPS).snoops().forEach(s -> {
                        if (Cytosis.CONTEXT.getComponent(SnooperManager.class).getChannel(Key.key(s)) == null) {
                            // big problem if null
                            Logger.warn("Player " + uuid + " is listening to the channel '" + s + "', but it isnt registered!");
                            Cytosis.getPlayer(uuid).ifPresent(player -> player.sendMessage(Msg.mm("<red><b>ERROR!</b></red><gray> Failed to start listening on snooper channel '" + s + "'")));
                        }
                    });
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
        persistPreferences(uuid, preferenceData.get(uuid));
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
            throw new IllegalArgumentException("Cannot set a preference of type " + value.getClass().getSimpleName() + " with a preference of type " + preference.value().getClass().getSimpleName());


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
    public <T> void updatePlayerPreference_UNSAFE(UUID uuid, Key preference, @Nullable T value) {
        TypedNamespace<?> typed = PREFERENCE_REGISTRY.typedNamespaces().stream().filter(t -> t.namespaceID().equals(preference)).findFirst().orElse(null);
        if (typed == null) throw new IllegalArgumentException("The preference " + preference + " does not exist!");
        if (value != null && typed.type() != value.getClass())
            throw new IllegalArgumentException("Cannot set a preference " + preference.asString() + " of type " + value.getClass().getSimpleName() + " with a preference of type " + typed.type().getSimpleName());
        updatePlayerPreference(uuid, (TypedNamespace<T>) typed, value);
    }

    /**
     * Gets the preference of a player
     *
     * @param uuid      The player
     * @param namespace the namespace
     * @param <T>       the type of the preference
     * @return the player's preference
     */
    @SuppressWarnings("unchecked")
    public <T> T getPlayerPreference_UNSAFE(UUID uuid, Key namespace) {
        TypedNamespace<?> typed = PREFERENCE_REGISTRY.typedNamespaces().stream().filter(t -> t.namespaceID().equals(namespace)).findFirst().orElse(null);
        if (typed == null) throw new IllegalArgumentException("The preference " + namespace + " does not exist!");
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
            throw new IllegalArgumentException("The preference " + namespaceID + " is not in the registry!");
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
            throw new IllegalArgumentException("The preference " + preference.namespace().asString() + " does not exist!");
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
        PreparedStatement ps = db.prepare("UPDATE cytonic_preferences SET preferences = ? WHERE uuid = ?;");
        ps.setString(2, uuid.toString());
        ps.setString(1, preferenceData.serialize());

        db.update(ps).whenComplete((unused, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst updating preferences!", throwable);
        });
    }

    @SneakyThrows
    public void addNewPlayerPreference(UUID uuid, PreferenceData data) {
        PreparedStatement ps = db.prepare("INSERT INTO cytonic_preferences VALUES(?,?);");
        ps.setString(1, uuid.toString());
        ps.setString(2, data.serialize());

        db.update(ps).whenComplete((unused, throwable) -> {
            if (throwable != null) Logger.error("An error occurred whilst updating preferences!", throwable);
        });
    }
}
