package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.Cytosis;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerUtils {

    /**
     * Attempts to resolve a player's UUID from a string. The string can be the player's nickname, username, or UUID. It is case-insensitive.
     *
     * @param input the player's nickname, username, or UUID.
     * @return the player's UUID, or null if the player could not be resolved.
     */
    public static @Nullable UUID resolveUuid(String input) {
        if (input == null) return null;
        UUID cached = Cytosis.getCytonicNetwork().getLifetimeFlattened().getByValue(input.toLowerCase());
        if (cached != null) {
            return cached;
        }
        return Cytosis.getNicknameManager().deanonymizePlayer(input);
    }

    /**
     * Attempts to resolve a player's name from their UUID.
     *
     * @param uuid the player's UUID.
     * @return the player's name, or null if the player could not be resolved.
     */
    public static @Nullable String resolveName(UUID uuid) {
        return Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid);
    }

}
