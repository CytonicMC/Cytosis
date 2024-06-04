package net.cytonic.cytosis.utils;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public record BanData(@Nullable String reason, @Nullable Instant expiry, boolean isBanned) {
    public BanData(@Nullable String reason, @Nullable Instant expiry, boolean isBanned) {
        this.reason = reason;
        this.expiry = expiry;
        this.isBanned = isBanned;
    }
}
