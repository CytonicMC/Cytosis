package net.cytonic.cytosis.server.chat;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;

public interface ChatService<P extends CytosisPlayer> {

    default void handleAllChat(P player, String originalMessage) {
        Component message = Component.text("").append(player.formattedName())
            .append(Component.text(":", player.getRank().getChatColor()))
            .appendSpace()
            .append(Component.text(originalMessage, player.getRank().getChatColor()));

        Cytosis.getOnlinePlayers().forEach((p) -> p.sendMessage(message));
    }
}
