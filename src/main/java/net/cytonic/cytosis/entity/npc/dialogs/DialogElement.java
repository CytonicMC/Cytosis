package net.cytonic.cytosis.entity.npc.dialogs;

public interface DialogElement {

    void run(Dialog dialog, int index);

    default void sendNextElement(Dialog dialog, int index) {
        dialog.sendElements(index + 1);
    }
}
