package net.cytonic.cytosis.data.enums;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.ProtocolObject;

/**
 * This enum holds all chat channels
 */
@Getter
public enum ChatChannel {
    /**
     * Public, single-server chat
     */
    ALL(Component.empty(), false, null),
    /**
     * Private messages between two players
     */
    PRIVATE_MESSAGE(Component.empty(), true, _ -> new HashSet<>()/* handled by chat manager */),
    /**
     * Represents an internal chat channel used for server-specific communication. This channel is not meant to be
     * exposed to or used by regular players.
     */
    INTERNAL_MESSAGE(Component.empty(), false, _ -> new HashSet<>() /*also handled by chat manager*/),

    PARTY(Msg.mm("<#83cae4>Party ></#83cae4> "), true, p -> {
        if (!p.isInParty()) return new HashSet<>();
        return Objects.requireNonNull(p.getParty()).getAllPlayers();
    }),
    /**
     * Not implemented; League chat
     */
    LEAGUE(Component.text("League > ", NamedTextColor.DARK_PURPLE), true, _ -> new HashSet<>()),
    /**
     * A chat channel broadcast to mods on every server
     */
    MOD(Component.text("Mod > ", NamedTextColor.DARK_GREEN), true, null),
    /**
     * A chat channel broadcast to admins on every server
     */
    ADMIN(Component.text("Admin > ", NamedTextColor.DARK_RED), true, null),
    /**
     * A chat channel broadcast to all staff on every server
     */
    STAFF(Component.text("Staff > ", NamedTextColor.LIGHT_PURPLE), true, null);

    private final Component prefix;
    private final boolean shouldDeanonymize;
    private final boolean supportsSelectiveRecipients;
    @Nullable
    private final Function<CytosisPlayer, Set<UUID>> recipientFunction;

    ChatChannel(Component prefix, boolean shouldDeanonymize, @Nullable Function<CytosisPlayer, Set<UUID>> recipients) {
        this.prefix = prefix;
        this.shouldDeanonymize = shouldDeanonymize;
        this.supportsSelectiveRecipients = recipients != null;
        this.recipientFunction = recipients;
    }
}