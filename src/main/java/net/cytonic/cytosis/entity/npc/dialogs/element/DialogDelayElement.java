package net.cytonic.cytosis.entity.npc.dialogs.element;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;

public record DialogDelayElement(int ticks) implements DialogElement {

    @Override
    public void run(Dialog dialog, int index) {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                sendNextElement(dialog, index))
            .delay(TaskSchedule.tick(ticks))
            .schedule();
    }
}
