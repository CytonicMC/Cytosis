package net.cytonic.cytosis.player;

import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.enums.PlayerRank;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class CytosisPlayer extends Player {
    private PlayerRank rank;

    public CytosisPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(uuid, username, playerConnection);
        rank = Cytosis.getRankManager().getPlayerRank(uuid).orElse(PlayerRank.DEFAULT); // todo: watch out for cache invalidations
    }

    public void setRank(PlayerRank rank) {
        Cytosis.getRankManager().changeRank(this, rank);
    }
}
