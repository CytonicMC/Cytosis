package net.cytonic.cytosis.data.enums;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.cytonic.cytosis.utils.Msg;

/**
 * This class holds constants that represent player ranks
 */
@Getter
public enum PlayerRank {
    // Staff ranks
    /**
     * The [OWNER] rank
     */
    OWNER(Msg.mm("<gold>[<dark_red>⛨</dark_red>]</gold> "), NamedTextColor.AQUA, NamedTextColor.WHITE),
    /**
     * The [ADMIN] rank
     */
    ADMIN(Msg.mm("<gold>[<red>⛨</red>]</gold> "), NamedTextColor.AQUA, NamedTextColor.WHITE),
    /**
     * The [MOD] rank
     */
    MODERATOR(Msg.mm("<gold>[<green>⛨</green>]</gold> "), NamedTextColor.AQUA, NamedTextColor.WHITE),
    /**
     * The [HELPER] rank
     */
    HELPER(Msg.mm("<gold>[<aqua>⛨</aqua>]</gold> "), NamedTextColor.AQUA, NamedTextColor.WHITE),

    // player ranks
    /**
     * The [NEXUS] rank
     */
    NEXUS(Msg.mm("<gold>[NEXUS]</gold> "), NamedTextColor.GOLD, NamedTextColor.WHITE),
    /**
     * The [SYNAPSE] rank
     */
    SYNAPSE(Msg.mm("<dark_green>[SYNAPSE]</dark_green> "), NamedTextColor.DARK_GREEN, NamedTextColor.WHITE),
    /**
     * The [CORTEX] rank
     */
    CORTEX(Msg.mm("<dark_purple>[CORTEX]</dark_purple> "), NamedTextColor.DARK_PURPLE, NamedTextColor.WHITE),
    /**
     * The [DEFAULT] rank
     */
    DEFAULT(Msg.mm(""), NamedTextColor.GRAY, NamedTextColor.GRAY);

    private final Component prefix;
    private final NamedTextColor teamColor;
    private final NamedTextColor chatColor;

    /**
     * Creates a Rank object
     *
     * @param prefix    The prefix shown in chat and above the player's head
     * @param teamColor The color of the player's name tag, and their name in chat
     * @param chatColor The color of the player's chat message
     */
    PlayerRank(Component prefix, NamedTextColor teamColor, NamedTextColor chatColor) {
        this.prefix = prefix;
        this.teamColor = teamColor;
        this.chatColor = chatColor;
    }

    /**
     * A method to check if a rank can be changed
     *
     * @param currentUserRole    The changer's current role
     * @param targetOriginalRole The player's original role
     * @param targetNewRole      The player's new role
     * @return if the rank can be changed
     */
    public static boolean canChangeRank(PlayerRank currentUserRole, PlayerRank targetOriginalRole,
        PlayerRank targetNewRole) {
        if (currentUserRole == OWNER) {
            return true;
        }
        if (isDemotion(targetOriginalRole, targetNewRole)) {
            return currentUserRole.ordinal() <= targetOriginalRole.ordinal();
        }
        if (isPromotion(targetOriginalRole, targetNewRole)) {
            return currentUserRole.ordinal() <= targetOriginalRole.ordinal();
        }
        // If it's neither promotion nor demotion, it's an invalid operation
        return false;
    }

    /**
     * A utility method to check if a demotion is valid
     *
     * @param currentRole The player's current role
     * @param newRole     The player's new role
     * @return Whether the demotion is valid
     */
    public static boolean isDemotion(PlayerRank currentRole, PlayerRank newRole) {
        return newRole.ordinal() > currentRole.ordinal();
    }

    /**
     * a method to check if a promotion is valid
     *
     * @param currentRole The player's current role
     * @param newRole     The player's new role
     * @return Whether the promotion is valid
     */
    public static boolean isPromotion(PlayerRank currentRole, PlayerRank newRole) {
        return newRole.ordinal() < currentRole.ordinal();
    }

    /**
     * a method to check if a promotion is valid
     *
     * @param newRole The player's new role
     * @return Whether the promotion is valid
     */
    public boolean isHigherOrEqualTo(PlayerRank newRole) {
        return newRole.ordinal() >= ordinal();
    }

    public boolean isStaff() {
        return this == OWNER || this == ADMIN || this == MODERATOR || this == HELPER;
    }

    public boolean isStaffNotHelper() {
        return this == ADMIN || this == MODERATOR || this == OWNER;
    }
}
