package net.cytonic.cytosis.snooper;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.utils.Utils;

/**
 * Represents a channel or pathway a message can be sent to.
 *
 * @param channel    The string representation of the channel.
 * @param id         The id of this snooper channel.
 * @param recipients The intended recipients of this snoop. Since there are 4 staff roles, 4 bits are unused.
 *                   <br>
 *                   {@link PlayerRank#OWNER} is {@code 0x01}
 *                   <br>
 *                   {@link PlayerRank#ADMIN} is {@code 0x02}
 *                   <br>
 *                   {@link PlayerRank#MODERATOR} is {@code 0x04}
 *                   <br>
 *                   {@link PlayerRank#HELPER} is {@code 0x08}
 */
public record SnooperChannel(String channel, Key id, byte recipients) {

    public static final Codec<SnooperChannel> CODEC = StructCodec.struct(
        "channel", Codec.STRING, SnooperChannel::channel,
        "id", Codec.KEY, SnooperChannel::id,
        "recipients", Codec.BYTE, SnooperChannel::recipients,
        SnooperChannel::new
    );

    public static SnooperChannel deserialize(String json) {
        return Utils.parseJson(json, CODEC);
    }

    public String serialize() {
        return Utils.toJson(this, CODEC);
    }
}
