package net.cytonic.cytosis.data.containers.snooper;

import net.cytonic.cytosis.data.enums.PlayerRank;
import net.minestom.server.utils.NamespaceID;

/**
 * @param channel
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
public record SnooperChannel(String channel, NamespaceID id, byte recipients) {
}
