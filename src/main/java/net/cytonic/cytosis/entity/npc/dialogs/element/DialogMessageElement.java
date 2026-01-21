package net.cytonic.cytosis.entity.npc.dialogs.element;


import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;
import net.cytonic.cytosis.utils.Msg;

public record DialogMessageElement(Component message) implements DialogElement {

    @Override
    public void run(Dialog dialog, int index) {
        if (dialog.isFinished()) return;
        Component message = dialog.getNpc().getName()
            .append(Msg.grey(": "))
            .append(message());
        dialog.getPlayer().sendMessage(message);
        sendNextElement(dialog, index);
    }
}
