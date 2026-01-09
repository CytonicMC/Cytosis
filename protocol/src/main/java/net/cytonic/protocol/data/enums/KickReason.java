package net.cytonic.protocol.data.enums;

import lombok.Getter;

/**
 * An enum holding data about kick reasons
 */
@Getter
public enum KickReason {
    /**
     * Kicked for being banned
     */
    BANNED(false),
    /**
     * Kicked for an internal error
     */
    INTERNAL_ERROR(true),
    /**
     * Kicked due to a world validation issue
     */
    INVALID_WORLD(true),
    /**
     * Kicked due to the kick command
     */
    COMMAND(false);

    private final boolean rescuable;

    /**
     * Creates a new KickReason
     *
     * @param rescuable if a proxy should try to rescue a player or terminate the connection completely
     */
    KickReason(boolean rescuable) {
        this.rescuable = rescuable;
    }
}