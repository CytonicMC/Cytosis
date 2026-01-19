package net.cytonic.cytosis.entity.npc.dialogs.element;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Range;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;

public record DialogDelayElement(@Range(from = 1, to = Integer.MAX_VALUE) int ticks) implements DialogElement {

    public DialogDelayElement {
        if (ticks <= 0) {
            throw new IllegalArgumentException("ticks must be positive");
        }
    }

    @Override
    public void run(Dialog dialog, int index) {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                sendNextElement(dialog, index))
            .delay(TaskSchedule.tick(ticks))
            .schedule();
    }
}
