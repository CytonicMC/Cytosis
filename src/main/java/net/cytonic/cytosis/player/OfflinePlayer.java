package net.cytonic.cytosis.player;

import java.util.Objects;
import java.util.UUID;

import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.player.trait.Cooldownable;
import net.cytonic.cytosis.player.trait.Preferable;

/**
 * A readonly representation of a player who is not connected to this server. While technically possible to mutate this
 * player's state, it is generally not advisable to do so from this object.
 * <p>
 * It is possible to create an {@link OfflinePlayer} for a player on this server, as the player's status is not
 * verified. The main use case of this class is reading preference data from a status-agnostic angle.
 */
public record OfflinePlayer(UUID uuid, String username, PlayerRank rank) implements Preferable, Cooldownable {

    public OfflinePlayer {
        Objects.requireNonNull(uuid, "UUID cannot be null!");
        Objects.requireNonNull(username, "Missing username!");
        Objects.requireNonNull(rank, "Missing rank!");
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }
}
