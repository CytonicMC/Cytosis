package net.cytonic.cytosis.server;

import dev.minestomunited.entrypoint.config.ConfigRegistry;
import dev.minestomunited.entrypoint.minestom.BasicMinestomService;
import dev.minestomunited.entrypoint.minestom.MinestomService;
import dev.minestomunited.entrypoint.minestom.player.MinestomPlayerService;
import dev.minestomunited.entrypoint.player.PlayerService;
import dev.minestomunited.entrypoint.server.AbstractMinestomServer;
import dev.minestomunited.entrypoint.session.SessionService;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minestom.server.Auth;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.server.chat.ChatService;
import net.cytonic.cytosis.server.player.PlayerServiceImpl;
import net.cytonic.cytosis.server.playerList.PlayerListService;
import net.cytonic.cytosis.server.session.SessionServiceImpl;
import net.cytonic.cytosis.server.sideboard.SideboardService;

@Getter
@Accessors(fluent = true)
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
        Cytosis.CONTEXT.registerComponent(getConfigOrThrow(CytosisConfig.class).environment());
    }

    @Override
    public Auth auth() {
        String secret = getConfigOrThrow(CytosisConfig.class).secret();
        if (Cytosis.isDev() && secret == null) {
            return new Auth.Online();
        }
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("Velocity secret is null or empty when not dev!");
        }
        return new Auth.Velocity(secret);
    }

    public abstract ChatService<P> chatService();

    public abstract PlayerListService<P> playerListService();

    public abstract SideboardService<P> sideboardService();

    public abstract void onShutdown();
}
