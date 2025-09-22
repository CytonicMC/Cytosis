package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.nicknames.NicknameManager;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;

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
        UUID cached = Cytosis.CONTEXT.getComponent(CytonicNetwork.class).getLifetimeFlattened().getByValue(input.toLowerCase());
        if (cached != null) {
            return cached;
        }
        return Cytosis.CONTEXT.getComponent(NicknameManager.class).deanonymizePlayer(input);
    }

    /**
     * Attempts to resolve a player's name from their UUID.
     *
     * @param uuid the player's UUID.
     * @return the player's name, or null if the player could not be resolved.
     */
    public static @Nullable String resolveName(UUID uuid) {
        return Cytosis.CONTEXT.getComponent(CytonicNetwork.class).getLifetimePlayers().getByKey(uuid);
    }
}