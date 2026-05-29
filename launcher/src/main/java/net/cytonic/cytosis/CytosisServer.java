package net.cytonic.cytosis;

import dev.minestomunited.entrypoint.config.ConfigRegistry;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.server.AbstractCytosisServer;
import net.cytonic.cytosis.server.chat.ChatService;
import net.cytonic.cytosis.server.chat.DefaultChatServiceImpl;
import net.cytonic.cytosis.server.playerList.PlayerListService;
import net.cytonic.cytosis.server.sideboard.SideboardService;

public class CytosisServer extends AbstractCytosisServer<CytosisPlayer> {

    private final ChatService<CytosisPlayer> chatService;
    private final PlayerListService<CytosisPlayer> playerListService;
    private final SideboardService<CytosisPlayer> sideboardService;

    protected CytosisServer(ConfigRegistry registry) {
        super(registry, CytosisPlayer::new);
        chatService = new DefaultChatServiceImpl<>();
        playerListService = new PlayerListService.Noop<>();
        sideboardService = new SideboardService.Noop<>();
    }

    @Override
    public ChatService<CytosisPlayer> chatService() {
        return chatService;
    }

    @Override
    public PlayerListService<CytosisPlayer> playerListService() {
        return playerListService;
    }

    @Override
    public SideboardService<CytosisPlayer> sideboardService() {
        return sideboardService;
    }

    @Override
    public void onShutdown() {
    }
}
