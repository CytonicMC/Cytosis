package net.cytonic.cytosis.data.packets;

import java.util.UUID;

/**
 * The packet for when the player logs in or logs out
 *
 * @param username the username of the player
 * @param uuid     the uuid of the player
 */
public record PlayerLoginLogoutPacket(String username, UUID uuid) implements Packet {
}
