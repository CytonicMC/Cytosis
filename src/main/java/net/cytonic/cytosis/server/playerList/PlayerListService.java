package net.cytonic.cytosis.server.playerList;

import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.playerlist.PlayerlistCreator;

public interface PlayerListService<P extends CytosisPlayer> {

    boolean supportsPlayerList();

    @Nullable
    PlayerlistCreator<P> creator();

    default int updateInterval() {
        return 20;
    }

    @SuppressWarnings("unused")
    class Noop<P extends CytosisPlayer> implements PlayerListService<P> {

        @Override
        public boolean supportsPlayerList() {
            return false;
        }

        @Override
        public @Nullable PlayerlistCreator<P> creator() {
            return null;
        }
    }
}
