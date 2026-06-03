package net.cytonic.cytosis.server.chat;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Preferences;

public class DefaultChatServiceImpl<P extends CytosisPlayer> implements ChatService<P> {

    @Override
    public void handleAllChat(P player, String originalMessage) {
        Component message = Component.text("").append(player.formattedName())
            .append(Component.text(":", player.getRank().getChatColor()))
            .appendSpace()
            .append(Component.text(originalMessage, player.getRank().getChatColor()));

        Cytosis.getOnlinePlayers().forEach((p) -> {
            if (p.getPreference(Preferences.IGNORED_CHAT_CHANNELS).all()) return;
            p.sendMessage(message);
        });
    }
}