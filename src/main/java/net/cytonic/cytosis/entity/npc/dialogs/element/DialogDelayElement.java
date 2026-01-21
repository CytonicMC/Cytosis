package net.cytonic.cytosis.entity.npc.dialogs.element;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Range;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;
import net.cytonic.cytosis.player.CytosisPlayer;

public record DialogDelayElement<P extends CytosisPlayer>(@Range(from = 1, to = Integer.MAX_VALUE) int ticks)
    implements DialogElement<P> {

    public DialogDelayElement {
        if (ticks <= 0) {
            throw new IllegalArgumentException("ticks must be positive");
        }
    }

    @Override
    public void run(P player, Dialog<P> dialog, int index) {
        if (dialog.isFinished()) return;
        MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (dialog.isFinished()) return;
                sendNextElement(player, dialog, index);
            })
            .delay(TaskSchedule.tick(ticks))
            .schedule();
    }
}
