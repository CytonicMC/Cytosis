package net.cytonic.cytosis.server.sideboard;

import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.sideboard.SideboardCreator;

public interface SideboardService<P extends CytosisPlayer> {

    boolean supportsSideboard();

    @Nullable
    SideboardCreator<P> sideboardCreator();

    default TaskSchedule scheduale() {
        return TaskSchedule.seconds(1);
    }

    @SuppressWarnings("unused")
    class Noop<P extends CytosisPlayer> implements SideboardService<P> {

        @Override
        public boolean supportsSideboard() {
            return false;
        }

        @Override
        public @Nullable SideboardCreator<P> sideboardCreator() {
            return null;
        }
    }
}
