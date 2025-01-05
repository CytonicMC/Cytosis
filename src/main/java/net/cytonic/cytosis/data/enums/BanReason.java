package net.cytonic.cytosis.data.enums;

import lombok.Getter;

/**
 * The reasons a player could be banned
 */
@Getter
public enum BanReason {
    /**
     * Cheating or hacking
     */
    CHEATING("Cheating/Hacking"),
    /**
     * A skin or cape that is not allowed
     */
    INAPROPRIATE_COSMETICS("Using a skin, cape, or other inappropriate cosmetics"),
    /**
     * Building something that violates the terms of service
     */
    INAPROPRIATE_BUILDING("Building a structure that violates our terms of service"),
    /**
     * Used when an accounts security is compromised
     */
    SECURITY("This account is suspended due to security concerns"),
    /**
     * Used for severe chat infractions
     */
    SEVERE_CHAT_INFRACTION("Severe chat infractions"),
    /**
     * Abusing bugs or exploits
     */
    EXPLOITING("Exploiting bugs or defects to your advantage"),
    /**
     * Scamming another user
     */
    SCAMMING("Violating our terms of service by attempting to scam another player");

    private final String reason;

    /**
     * A constructor for BanReason
     *
     * @param reason The reason shown to the player
     */
    BanReason(String reason) {
        this.reason = reason;
    }
}