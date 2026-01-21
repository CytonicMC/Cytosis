package net.cytonic.cytosis.entity.npc.dialogs.element;

import java.util.function.Consumer;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;

public record DialogActionElement(Consumer<Dialog> consumer) implements DialogElement {

    @Override
    public void run(Dialog dialog, int index) {
        if (dialog.isFinished()) return;
        consumer.accept(dialog);
        sendNextElement(dialog, index);
    }
}
