package net.cytonic.cytosis.data.enums;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * This enum holds all chat channels
 */
@Getter
public enum ChatChannel {
    /**
     * Public, single-server chat
     */
    ALL(Component.empty()),
    /**
     * Not implementated; Private messages between two players
     */
    PRIVATE_MESSAGE(Component.empty()),
    /**
     * Not implementated; Party chat
     */
    PARTY(Component.text("Party > ", NamedTextColor.GOLD)),
    /**
     * Not implementated; League chat
     */
    LEAGUE(Component.text("League > ", NamedTextColor.DARK_PURPLE)),
    /**
     * A chat channel broadcast to mods on every server
     */
    MOD(Component.text("Mod > ", NamedTextColor.DARK_GREEN)),
    /**
     * A chat channel broadcast to admins on every server
     */
    ADMIN(Component.text("Admin > ", NamedTextColor.DARK_RED)),
    /**
     * A chat channel broadcast to all staff on every server
     */
    STAFF(Component.text("Staff > ", NamedTextColor.LIGHT_PURPLE));

    private final Component prefix;
    ChatChannel(Component prefix) {this.prefix = prefix;}
}