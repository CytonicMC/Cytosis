package net.cytonic.cytosis.server;

import dev.minestomunited.entrypoint.config.ConfigRegistry;
import dev.minestomunited.entrypoint.minestom.BasicMinestomService;
import dev.minestomunited.entrypoint.minestom.MinestomService;
import dev.minestomunited.entrypoint.minestom.player.MinestomPlayerService;
import dev.minestomunited.entrypoint.player.PlayerService;
import dev.minestomunited.entrypoint.server.AbstractMinestomServer;
import dev.minestomunited.entrypoint.session.SessionService;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.server.chat.ChatService;
import net.cytonic.cytosis.server.player.PlayerServiceImpl;
import net.cytonic.cytosis.server.playerList.PlayerListService;
import net.cytonic.cytosis.server.session.SessionServiceImpl;
import net.cytonic.cytosis.server.sideboard.SideboardService;

public abstract class AbstractCytosisServer<P extends CytosisPlayer> extends AbstractMinestomServer {

    private final SessionService sessionService;
    private final PlayerService playerService;
    private final MinestomService<P> minestomService;

    protected AbstractCytosisServer(ConfigRegistry registry,
        MinestomPlayerService.MinestomPlayerProvider<P> playerProvider) {
        super(registry);
        sessionService = new SessionServiceImpl();
        playerService = new PlayerServiceImpl();
        minestomService = new BasicMinestomService<>(registry, sessionService, playerService, playerProvider);
    }

    @Override
    public SessionService sessionService() {
        return sessionService;
    }

    @Override
    public PlayerService playerService() {
        return playerService;
    }

    @Override
    public MinestomService<?> minestomService() {
        return minestomService;
    }

    public abstract ChatService<P> chatService();

    public abstract PlayerListService<P> playerListService();

    public abstract SideboardService<P> sideboardService();

    public abstract void onShutdown();
}
