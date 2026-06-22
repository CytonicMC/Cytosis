package net.cytonic.cytosis.server.actionBar;

import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.actionBar.ActionBarCreator;
import net.cytonic.cytosis.player.CytosisPlayer;

public interface ActionBarService<P extends CytosisPlayer> {

    boolean supportsActionBar();

    @Nullable
    ActionBarCreator<P> actionBarCreator();

    @SuppressWarnings("unused")
    class Noop<P extends CytosisPlayer> implements ActionBarService<P> {

        @Override
        public boolean supportsActionBar() {
            return false;
        }

        @Override
        public @Nullable ActionBarCreator<P> actionBarCreator() {
            return null;
        }
    }
}
