package net.cytonic.cytosis.utils;

import java.util.UUID;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.nicknames.NicknameManager.NicknameData;
import net.cytonic.cytosis.player.OfflinePlayer;

@UtilityClass
public class Players {

    /**
     * Gets a readonly object representing this player. This is safe to call if the player is offline or online.
     *
     * @param target the UUID of the target player
     * @return the readonly player object with accessors for preferences and cooldowns
     * @throws NullPointerException if {@code target} is null, or points to a player who has never logged in.
     */
    @NotNull
    public static OfflinePlayer offline(UUID target) {
        return new OfflinePlayer(target,
            network().getLifetimePlayers().getByKey(target),
            network().getCachedPlayerRanks().get(target)
        );
    }

    /**
     * Gets a readonly object representing this player. This is safe to call if the player is offline or online.
     *
     * @param target the NAME of the target player
     * @return the readonly player object with accessors for preferences and cooldowns
     * @throws NullPointerException if {@code target} is null, or points to a player who has never logged in.
     */
    @NotNull
    public static OfflinePlayer offline(String target) {
        return offline(resolveUuid(target));
    }

    /**
     * Gets the MiniMessage formatted name of this player as a normal player would see it. This means that if the player
     * is nicked, the nickname is always returned. Use {@link #miniNameFragile(String)} where deanonymization is a
     * problem.
     *
     * @param player The UUID of the player to represent
     * @return the MiniMessage formatted player name. The rank is included, and all MiniMessage tags are terminated.
     * Thus, no style leakage occurs as a result.
     */
    public static String miniName(UUID player) {
        PlayerRank rank;
        String name;

        NicknameData data = Cytosis.get(NicknameManager.class).getData(player);
        if (data != null) {
            rank = data.rank();
            name = data.nickname();
        } else {
            rank = network().getCachedPlayerRanks().get(player);
            name = network().getLifetimePlayers().getByKey(player);
        }

        String color = rank.getTeamColor().toString();
        return Msg.toMini(rank.getPrefix()) + String.format("<%s>%s</%s>", color, name, color);
    }

    /**
     * Just like {@link #miniName(UUID)}, but it always returns the player's real identity.
     */
    public static String trueMiniName(UUID player) {
        PlayerRank rank = network().getCachedPlayerRanks().get(player);
        String name = network().getLifetimePlayers().getByKey(player);

        String color = rank.getTeamColor().toString();
        return Msg.toMini(rank.getPrefix()) + String.format("<%s>%s</%s>", color, name, color);
    }

    /**
     * Gets the player's name and rank formatted like [Nexus] Foxikle, except it does not reveal a player's nicked
     * status. If a nicked player's true username is entered, it returns the player's true rank. If the player's nicked
     * username is entered, then it returns the player's nicked rank.
     *
     * @param input The player's USERNAME to format. Unlike {@link #miniName(UUID)}, this method only works with
     *              Username, as the UUID doesn't provide the context if the requesting player sees the real identity or
     *              not.
     * @return The formatted name, not revealing
     */
    @Nullable
    public static String miniNameFragile(String input) {
        PlayerRank rank;
        String name;

        UUID uuid = resolveNickedUuid(input);
        if (uuid != null) {
            NicknameData data = Cytosis.get(NicknameManager.class).getData(uuid);
            if (data != null) {
                rank = data.rank();
                name = data.nickname();
            } else {
                return null;
            }
        } else {
            uuid = resolveUnickedUuid(input);
            if (uuid == null) return null;
            rank = network().getCachedPlayerRanks().get(uuid);
            name = network().getLifetimePlayers().getByKey(uuid);
        }

        String color = rank.getTeamColor().toString();
        return Msg.toMini(rank.getPrefix()) + String.format("<%s>%s</%s>", color, name, color);
    }

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

        UUID local = network().getLifetimeFlattened()
            .getByValue(input.toLowerCase());
        if (local != null) return local;
        return nickname().deanonymizePlayer(input);
    }

    /**
     * Resolves the nicked player from the given input.
     *
     * @param playerName The input to try to parse
     * @return The resolved UUID, if and only if the player is nicknamed, potentially null.
     */
    public static @Nullable UUID resolveNickedUuid(String playerName) {
        return nickname().deanonymizePlayer(playerName);
    }

    /**
     * Resolves the unnicked player from the given input.
     *
     * @param playerName The input to try to parse
     * @return The resolved UUID, if and only if the player is NOT nicknamed, potentially null.
     */
    public static @Nullable UUID resolveUnickedUuid(String playerName) {
        return network().getLifetimeFlattened().getByValue(playerName.toLowerCase());
    }

    /**
     * Attempts to resolve a player's name from their UUID. This will return their nickname if they are nicked.
     *
     * @param uuid the player's UUID.
     * @return the player's name, or null if the player could not be resolved.
     */
    public static @Nullable String resolveName(UUID uuid) {
        NicknameData d = nickname().getData(uuid);
        if (d != null) {
            return d.nickname();
        }
        return resolveTrueName(uuid);
    }

    /**
     * Attempts to resolve a player's name from their UUID.
     *
     * @param uuid the player's UUID.
     * @return the player's name, or null if the player could not be resolved.
     */
    public static @Nullable String resolveTrueName(UUID uuid) {
        return network().getLifetimePlayers().getByKey(uuid);
    }

    private static CytonicNetwork network() {
        return Cytosis.get(CytonicNetwork.class);
    }

    private static NicknameManager nickname() {
        return Cytosis.get(NicknameManager.class);
    }
}
