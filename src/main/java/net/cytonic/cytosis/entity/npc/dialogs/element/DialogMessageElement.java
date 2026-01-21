package net.cytonic.cytosis.entity.npc.dialogs.element;


import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public record DialogMessageElement<P extends CytosisPlayer>(Component message)
    implements DialogElement<P> {

    @Override
    public void run(P player, Dialog<P> dialog, int index) {
        if (dialog.isFinished()) return;
        Component message = dialog.getNpc().getName()
            .append(Msg.grey(": "))
            .append(message());
        player.sendMessage(message);
        sendNextElement(player, dialog, index);
    }
}
