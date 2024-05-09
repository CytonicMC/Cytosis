package net.cytonic.cytosis.events.ranks;

import lombok.Getter;
import net.cytonic.cytosis.ranks.PlayerRank;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;

@Getter
public class RankChangeEvent implements PlayerEvent, CancellableEvent {
    private boolean canceled;
    private final PlayerRank newRank;
    private final PlayerRank oldRank;
    private final Player player;

    public RankChangeEvent(PlayerRank newRank, PlayerRank oldRank, Player player) {
        this.newRank = newRank;
        this.oldRank = oldRank;
        this.player = player;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }
}
