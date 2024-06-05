package net.cytonic.cytosis.enums;

import lombok.Getter;

@Getter
public enum BanReason {
    CHEATING("Cheating/Hacking"),
    INAPROPRIATE_COSMETICS("Using a skin, cape, or other inappropriate cosmetics"),
    INAPROPRIATE_BUILDING("Building a structure that violates our terms of service"),
    SECURITY("This account is suspended due to security concerns"),
    SEVERE_CHAT_INFRACTION("Severe chat infractions"),
    EXPLOITING("Exploiting bugs or defects to your advantage"),
    SCAMMING("Violating our terms of service by attempting to scam another player"),
    ;

    private final String reason;

    BanReason(String reason) {
        this.reason = reason;
    }
}
