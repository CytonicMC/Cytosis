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
    ALL(Component.empty(), false, false),
    /**
     * Private messages between two players
     */
    PRIVATE_MESSAGE(Component.empty(), true, true),
    /**
     * Represents an internal chat channel used for server-specific communication. This channel is not meant to be
     * exposed to or used by regular players.
     */
    INTERNAL_MESSAGE(Component.empty(), false, true),
    /**
     * Not implementated; Party chat
     */
    PARTY(Component.text("Party > ", NamedTextColor.GOLD), false, false),
    /**
     * Not implementated; League chat
     */
    LEAGUE(Component.text("League > ", NamedTextColor.DARK_PURPLE), true, false),
    /**
     * A chat channel broadcast to mods on every server
     */
    MOD(Component.text("Mod > ", NamedTextColor.DARK_GREEN), true, false),
    /**
     * A chat channel broadcast to admins on every server
     */
    ADMIN(Component.text("Admin > ", NamedTextColor.DARK_RED), true, false),
    /**
     * A chat channel broadcast to all staff on every server
     */
    STAFF(Component.text("Staff > ", NamedTextColor.LIGHT_PURPLE), true, false);

    private final Component prefix;
    private final boolean shouldDeanonymize;
    private final boolean supportsSelectiveRecipients;

    ChatChannel(Component prefix, boolean shouldDeanonymize, boolean supportsSelectiveRecipients) {
        this.prefix = prefix;
        this.shouldDeanonymize = shouldDeanonymize;
        this.supportsSelectiveRecipients = supportsSelectiveRecipients;
    }
}