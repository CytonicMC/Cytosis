package net.cytonic.cytosis.entity.npc.dialogs;

public interface DialogElement {

    void run(Dialog dialog, int index);

    default void sendNextElement(Dialog dialog, int index) {
        sendElements(dialog, index + 1);
    }

    default void sendElements(Dialog dialog, int index) {
        dialog.sendElements(index);
    }
}
