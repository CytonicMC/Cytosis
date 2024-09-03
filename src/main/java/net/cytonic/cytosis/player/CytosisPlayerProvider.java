package net.cytonic.cytosis.player;

import net.minestom.server.entity.Player;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The provider for Minestom
 */
public class CytosisPlayerProvider implements PlayerProvider {

    /**
     * @param uuid       the player {@link UUID}
     * @param username   the player username
     * @param connection the player connection
     * @return the created Player
     */
    @Override
    public @NotNull Player createPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection connection) {
        return new CytosisPlayer(uuid, username, connection);
    }
}
