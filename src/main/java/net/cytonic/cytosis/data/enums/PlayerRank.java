package net.cytonic.cytosis.data.enums;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.color.TeamColor;

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
    OWNER(Msg.mm("<gold>[<dark_red>⛨</dark_red>]</gold> "), TeamColor.AQUA, "<white>"),
    /**
     * The [ADMIN] rank
     */
    ADMIN(Msg.mm("<gold>[<red>⛨</red>]</gold> "), TeamColor.AQUA, "<white>"),
    /**
     * The [MOD] rank
     */
    MODERATOR(Msg.mm("<gold>[<green>⛨</green>]</gold> "), TeamColor.AQUA, "<white>"),
    /**
     * The [HELPER] rank
     */
    HELPER(Msg.mm("<gold>[<aqua>⛨</aqua>]</gold> "), TeamColor.AQUA, "<white>"),

    // player ranks
    /**
     * The [NEXUS] rank
     */
    NEXUS(Msg.mm("<gold>☆</gold> "), TeamColor.GOLD, "<white>"),
    /**
     * The [SYNAPSE] rank
     */
    SYNAPSE(Msg.mm("<dark_green>\uD83D\uDC09</dark_green> "), TeamColor.DARK_GREEN, "<white>"),
    /**
     * The [CORTEX] rank
     */
    CORTEX(Msg.mm("<dark_purple>✞</dark_purple> "), TeamColor.DARK_PURPLE, "<white>"),
    /**
     * The [DEFAULT] rank
     */
    DEFAULT(Msg.mm(""), TeamColor.GRAY, "<gray>");
    public static final Codec<PlayerRank> CODEC = Codec.Enum(PlayerRank.class);
    private final Component prefix;
    private final TeamColor teamColor;
    private final String chatColor;

    /**
     * Creates a Rank object
     *
     * @param prefix    The prefix shown in chat and above the player's head
     * @param teamColor The color of the player's name tag, and their name in chat
     * @param chatColor The color of the player's chat message
     */
    PlayerRank(Component prefix, TeamColor teamColor, String chatColor) {
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
