package net.cytonic.cytosis.ranks;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

/**
 * This class holds constants that represent player ranks
 */
@Getter
public enum PlayerRank {
    // Staff ranks
    /**
     * The [OWNER] rank
     */
    OWNER(MM."<red>[OWNER]", NamedTextColor.RED, NamedTextColor.WHITE, new String[]{"*"}),
    /**
     * The [ADMIN] rank
     */
    ADMIN(MM."<red>[ADMIN]", NamedTextColor.RED, NamedTextColor.WHITE, new String[]{"*"}),
    /**
     * The [MOD] rank
     */
    MODERATOR(MM."<green>[MOD]", NamedTextColor.GREEN, NamedTextColor.WHITE, new String[]{"cytonic.chat.staff", "cytosis.commands.fly"}),
    /**
     * The [HELPER] rank
     */
    HELPER(MM."<aqua>[HELPER]", NamedTextColor.AQUA, NamedTextColor.WHITE, new String[]{"cytonic.chat.staff", "cytosis.commands.fly"}),

    // player ranks
    /**
     * The [ELYSIAN] rank
     */
    ELYSIAN(MM."<gold>[ELYSIAN]", NamedTextColor.GOLD, NamedTextColor.WHITE, new String[]{"cytosis.commands.fly"}),
    /**
     * The [CELESTIAL] rank
     */
    CELESTIAL(MM."<dark_aqua>[CELESTIAL]", NamedTextColor.DARK_AQUA, NamedTextColor.WHITE, new String[]{"cytosis.commands.fly"}),
    /**
     * The [MASTER] rank
     */
    MASTER(MM."<dark_red>[MASTER]", NamedTextColor.DARK_RED, NamedTextColor.WHITE, new String[]{"cytosis.commands.fly"}),
    /**
     * The [VALIENT] rank
     */
    VALIENT(MM."<dark_green>[VALIENT]", NamedTextColor.DARK_GREEN, NamedTextColor.WHITE, new String[]{"cytosis.commands.fly"}),
    /**
     * The [NOBLE] rank
     */
    NOBLE(MM."<dark_purple>[NOBLE]", NamedTextColor.DARK_PURPLE, NamedTextColor.WHITE, new String[]{"cytosis.commands.fly"}),
    /**
     * The [DEFAULT] rank
     */
    DEFAULT(MM."<GRAY>[DEFAULT]", NamedTextColor.GRAY, NamedTextColor.GRAY, new String[]{});

    private final Component prefix;
    private final NamedTextColor teamColor;
    private final NamedTextColor chatColor;
    private final String[] permissions;

    /**
     * Creates a Rank object
     *
     * @param prefix      The prefix shown in chat and above the player's head
     * @param teamColor   The color of the player's name tag, and their name in chat
     * @param chatColor   The color of the player's chat message
     * @param permissions The permissions that rank has
     */
    PlayerRank(Component prefix, NamedTextColor teamColor, NamedTextColor chatColor, String[] permissions) {
        this.prefix = prefix;
        this.teamColor = teamColor;
        this.chatColor = chatColor;
        this.permissions = permissions;
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
     * A method to check if a rank can be changed
     *
     * @param currentUserRole    The changer's current role
     * @param targetOriginalRole The player's original role
     * @param targetNewRole      The player's new role
     * @return if the rank can be changed
     */
    public static boolean canChangeRank(PlayerRank currentUserRole, PlayerRank targetOriginalRole, PlayerRank targetNewRole) {
        if (currentUserRole == OWNER) return true;
        if (isDemotion(targetOriginalRole, targetNewRole)) {
            return currentUserRole.ordinal() <= targetOriginalRole.ordinal();
        }
        if (isPromotion(targetOriginalRole, targetNewRole)) {
            return currentUserRole.ordinal() <= targetOriginalRole.ordinal();
        }
        // If it's neither promotion nor demotion, it's an invalid operation
        return false;
    }
}
