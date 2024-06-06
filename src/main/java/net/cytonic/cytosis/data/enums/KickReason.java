package net.cytonic.cytosis.data.enums;

import lombok.Getter;

@Getter
public enum KickReason {
    BANNED(false),
    INTERNAL_ERROR(true),
    INVALID_WORLD(true),
    ;

    private final boolean rescuable;

    KickReason(boolean rescuable) {
        this.rescuable = rescuable;
    }

}