package net.cytonic.cytosis.data.objects;

import java.time.Instant;

import dev.minestomunited.common.codecUtils.CodecUtils;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

/**
 * A Record that contains information about a player's ban
 *
 * @param reason   The reason they were banned
 * @param expiry   The Instant when they will be unbanned
 * @param isBanned If the player is banned
 */
public record BanData(@Nullable String reason, @Nullable Instant expiry, boolean isBanned) {

    public static final Codec<BanData> CODEC = StructCodec.struct(
        "reason", Codec.STRING.optional(), BanData::reason,
        "expiry", CodecUtils.INSTANT.optional(), BanData::expiry,
        "isBanned", Codec.BOOLEAN, BanData::isBanned,
        BanData::new
    );
}
