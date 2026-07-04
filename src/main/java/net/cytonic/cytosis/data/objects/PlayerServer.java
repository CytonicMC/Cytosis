package net.cytonic.cytosis.data.objects;

import java.util.UUID;

/**
 * Represents a player connected to a specific server.
 *
 * @param uuid   The uuid of the player.
 * @param server The server the player is connected to.
 */
public record PlayerServer(UUID uuid, CytonicServer server) {

}
