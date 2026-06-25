package net.cytonic.cytosis.server.chat;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

public class DefaultChatServiceImpl<P extends CytosisPlayer> implements ChatService<P> {

    @Override
    public void handleAllChat(P player, String originalMessage) {
        ChatManager manager = Cytosis.get(ChatManager.class);
        PlayerRank r = player.getRank();

        String msg = manager.translateEmojis(originalMessage, r);
        msg = r.getChatColor() + ": " + msg;
        Component message = player.formattedName().append(Msg.mm(msg));
        Cytosis.getOnlinePlayers().forEach((p) -> {
            if (p.getPreference(Preferences.IGNORED_CHAT_CHANNELS).all()) return;
            p.sendMessage(message);
        });
    }
}