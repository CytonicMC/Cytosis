package net.cytonic.cytosis.player.trait;

import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.key.Key;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.objects.preferences.ToggleablePreference;
import net.cytonic.cytosis.managers.PreferenceManager;

public interface Preferable {

    private static PreferenceManager preferences() {
        return Cytosis.get(PreferenceManager.class);
    }

    UUID getUuid();

    /**
     * Updates the preference
     *
     * @param namespace the namespace of the preference
     * @param value     the value to set the preference
     * @param <T>       the type of the preference value
     * @throws IllegalArgumentException if the preference has not been registered with the {@link PreferenceManager}
     * @throws IllegalArgumentException if the preference and value are not of the same type
     */
    default <T> void updatePreference(Preference<T> namespace, T value) {
        preferences().updatePlayerPreference(getUuid(), namespace, value);
    }

    default void togglePreference(ToggleablePreference preference, Runnable onTrue, Runnable onFalse) {
        boolean value = !getPreference(preference);
        if (value) {
            onTrue.run();
        } else {
            onFalse.run();
        }
        preferences().updatePlayerPreference(getUuid(), preference, value);
    }

    /**
     * Gets all the possible preferences this player has. Some may not be registered in the {@link PreferenceManager}.
     *
     * @return the value stored in the preference
     */
    default Set<Key> getPreferenceKeys() {
        return preferences().getPlayerKeys(getUuid());
    }

    /**
     * Gets a preference value
     *
     * @param namespace the namespace of the preference
     * @param <T>       the Type of the preference
     * @return the value stored in the preference
     * @throws IllegalArgumentException if the preference has not been registered with the {@link PreferenceManager}
     */
    default <T> T getPreference(Preference<T> namespace) {
        return preferences().getPlayerPreference(getUuid(), namespace);
    }

}
