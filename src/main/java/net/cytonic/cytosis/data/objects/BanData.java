package net.cytonic.cytosis.data.objects;

import java.time.Instant;

import org.jetbrains.annotations.Nullable;

/**
 * A Record that contains information about a player's ban
 *
 * @param reason   The reason they were banned
 * @param expiry   The Instant when they will be unbanned
 * @param isBanned If the player is banned
 */
public record BanData(@Nullable String reason, @Nullable Instant expiry, boolean isBanned) {

}
