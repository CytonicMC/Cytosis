package net.cytonic.cytosis.server.chat;

import net.cytonic.cytosis.player.CytosisPlayer;

public interface ChatService<P extends CytosisPlayer> {

    void handleAllChat(P player, String originalMessage);

    class Noop<P extends CytosisPlayer> implements ChatService<P> {

        @Override
        public void handleAllChat(P player, String originalMessage) {
        }
    }
}
