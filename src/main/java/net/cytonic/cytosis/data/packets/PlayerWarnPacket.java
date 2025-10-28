package net.cytonic.cytosis.data.packets;

import java.util.UUID;

/**
 * The packet for when a player gets warned
 *
 * @param target      the players uuid
 * @param warnMessage the warning message
 */
public record PlayerWarnPacket(UUID target, String warnMessage) implements Packet {
}
