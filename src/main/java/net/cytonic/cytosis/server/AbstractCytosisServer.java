package net.cytonic.cytosis.server;

import java.net.URL;
import java.util.List;

import dev.minestomunited.common.config.ConfigRegistry;
import dev.minestomunited.entrypoint.minestom.BasicMinestomService;
import dev.minestomunited.entrypoint.minestom.MinestomService;
import dev.minestomunited.entrypoint.minestom.player.MinestomPlayerService;
import dev.minestomunited.entrypoint.player.PlayerService;
import dev.minestomunited.entrypoint.server.AbstractMinestomServer;
import dev.minestomunited.entrypoint.session.SessionService;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.minestom.server.Auth;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.server.actionBar.ActionBarService;
import net.cytonic.cytosis.server.chat.ChatService;
import net.cytonic.cytosis.server.menu.MenuService;
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
        minestomService = new BasicMinestomService<>(this, registry, sessionService, playerService, playerProvider);
        Cytosis.CONTEXT.registerComponent(getConfigOrThrow(CytosisConfig.class).environment());
    }

    @Override
    public Auth auth() {
        if (isStandalone()) {
            return new Auth.Online();
        }

        String secret = getConfigOrThrow(CytosisConfig.class).secret();
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("Velocity secret is null or empty!");
        }
        return new Auth.Velocity(secret);
    }

    @Override
    public boolean isStandalone() {
        return getConfigOrThrow(CytosisConfig.class).standalone();
    }

    public abstract Key serverType();

    public abstract void onShutdown();

    @OverrideOnly
    public List<URL> extraJandexFiles() {
        return List.of();
    }

    public abstract ChatService<P> chatService();

    public abstract PlayerListService<P> playerListService();

    public abstract SideboardService<P> sideboardService();

    public abstract ActionBarService<P> actionBarService();

    public abstract MenuService menuService();
}
