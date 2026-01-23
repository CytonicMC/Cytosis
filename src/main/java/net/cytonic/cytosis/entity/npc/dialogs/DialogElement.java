package net.cytonic.cytosis.entity.npc.dialogs;

import net.cytonic.cytosis.player.CytosisPlayer;

public interface DialogElement<P extends CytosisPlayer> {

    void run(P player, Dialog<P> dialog, int index);

    default void sendNextElement(P player, Dialog<P> dialog, int index) {
        dialog.sendElements(player, index + 1);
    }
}
