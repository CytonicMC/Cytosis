package net.cytonic.cytosis.auditlog;

/**
 * An enum for the different audit log categories
 */
public enum Category {
    /**
     * A player ban
     */
    BAN,
    /**
     * A player unban
     */
    UNBAN,
    /**
     * A player mute
     */
    MUTE,
    /**
     * A player unmute
     */
    UNMUTE,
    /**
     * A player IP ban
     */
    IPBAN,
    /**
     * A player IP unban
     */
    IPUNBAN,
    /**
     * A player kick
     */
    KICK,
    /**
     * A player warn
     */
    WARN
}