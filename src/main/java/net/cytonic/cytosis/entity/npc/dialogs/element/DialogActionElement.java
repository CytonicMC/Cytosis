package net.cytonic.cytosis.entity.npc.dialogs.element;

import java.util.function.BiConsumer;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;
import net.cytonic.cytosis.player.CytosisPlayer;

public record DialogActionElement<P extends CytosisPlayer>(BiConsumer<P, Dialog<P>> consumer) implements
    DialogElement<P> {

    @Override
    public void run(P player, Dialog<P> dialog, int index) {
        if (dialog.isFinished()) return;
        consumer.accept(player, dialog);
        sendNextElement(player, dialog, index);
    }
}
