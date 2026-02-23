package net.cytonic.cytosis.utils;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.nicknames.NicknameManager;

public class PlayerUtils {

    /**
     * Attempts to resolve a player's UUID from a string. The string can be the player's nickname, username, or UUID. It
     * is case-insensitive.
     *
     * @param input the player's nickname, username, or UUID.
     * @return the player's UUID, or null if the player could not be resolved.
     */
    public static @Nullable UUID resolveUuid(String input) {
        if (input == null) {
            return null;
        }
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ignored) {
        }

        UUID local = Cytosis.get(CytonicNetwork.class).getLifetimeFlattened()
            .getByValue(input.toLowerCase());
        if (local != null) return local;
        return Cytosis.get(NicknameManager.class).deanonymizePlayer(input);
    }

    /**
     * Resolves the nicked player from the given input.
     *
     * @param playerName The input to try to parse
     * @return The resolved UUID, if and only if the player is nicknamed, potentially null.
     */
    public static @Nullable UUID resolveNickedUuid(String playerName) {
        return Cytosis.get(NicknameManager.class).deanonymizePlayer(playerName);
    }

    /**
     * Resolves the nicked player from the given input.
     *
     * @param playerName The input to try to parse
     * @return The resolved UUID, if and only if the player is NOT nicknamed, potentially null.
     */
    public static @Nullable UUID resolveUnickedUuid(String playerName) {
        return Cytosis.get(CytonicNetwork.class).getLifetimeFlattened()
            .getByValue(playerName.toLowerCase());
    }

    /**
     * Attempts to resolve a player's name from their UUID. This will return their nickname if they are nicked.
     *
     * @param uuid the player's UUID.
     * @return the player's name, or null if the player could not be resolved.
     */
    public static @Nullable String resolveName(UUID uuid) {
        if (Cytosis.get(NicknameManager.class).getData(uuid) != null) {
            return Cytosis.get(NicknameManager.class).getData(uuid).nickname();
        }
        return Cytosis.get(CytonicNetwork.class).getLifetimePlayers().getByKey(uuid);
    }

    /**
     * Attempts to resolve a player's name from their UUID.
     *
     * @param uuid the player's UUID.
     * @return the player's name, or null if the player could not be resolved.
     */
    public static @Nullable String resolveTrueName(UUID uuid) {
        return Cytosis.get(CytonicNetwork.class).getLifetimePlayers().getByKey(uuid);
    }
}