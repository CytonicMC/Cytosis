package net.cytonic.cytosis.data.packets;

import net.cytonic.cytosis.data.enums.KickReason;

import java.util.UUID;

/**
 * The packet for when a player gets kicked
 *
 * @param uuid        the player's uuid
 * @param reason      the reason for the kick
 * @param kickMessage the message to send to the player when they are kicked
 */
public record PlayerKickPacket(UUID uuid, KickReason reason, String kickMessage) implements Packet {
}
