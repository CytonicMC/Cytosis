package net.cytonic.cytosis.server;

import dev.minestomunited.common.config.ConfigRegistry;
import dev.minestomunited.entrypoint.config.ServerConfig;
import dev.minestomunited.entrypoint.minestom.BasicMinestomService;
import dev.minestomunited.entrypoint.minestom.MinestomService;
import dev.minestomunited.entrypoint.minestom.player.MinestomPlayerService;
import dev.minestomunited.entrypoint.minestom.player.NetworkPlayer;
import dev.minestomunited.entrypoint.player.PlayerService;
import dev.minestomunited.entrypoint.server.MinestomServer;
import dev.minestomunited.entrypoint.session.SessionService;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.UnknownNullability;

import net.cytonic.cytosis.StaticInitializers;

/**
 * Essentially the same as {@link BasicMinestomService<P>}, but it doesn't initialize minestom, since it is initialized
 * during the native image build
 *
 * @param <P>
 */
public class NativeMinestomService<P extends Player & NetworkPlayer> implements MinestomService<P> {

    private final MinestomServer minestomServer;
    private final ConfigRegistry registry;
    private final MinestomPlayerService.MinestomPlayerProvider<P> playerProvider;
    private final SessionService sessionService;
    private final PlayerService playerService;
    @UnknownNullability
    private final MinecraftServer server = StaticInitializers.SERVER;
    @UnknownNullability
    private MinestomPlayerService<P> minestomPlayerService = null;

    /**
     * Creates a new service backed by the given registry and supporting services.
     *
     * @param registry       the config registry used to resolve startup configuration
     * @param sessionService the session service for tracking online players
     * @param playerService  the player service for loading and persisting player data
     * @param playerProvider factory that instantiates the player object per connection
     */
    public NativeMinestomService(
        MinestomServer minestomServer,
        ConfigRegistry registry,
        SessionService sessionService,
        PlayerService playerService,
        MinestomPlayerService.MinestomPlayerProvider<P> playerProvider) {
        this.minestomServer = minestomServer;
        this.registry = registry;
        this.sessionService = sessionService;
        this.playerService = playerService;
        this.playerProvider = playerProvider;
    }

    @Override
    public void setup(Auth auth) {
        if (minestomPlayerService != null) {
            throw new IllegalStateException("server already setup");
        }
        minestomPlayerService = new MinestomPlayerService<>(minestomServer, eventNode(), sessionService, playerService,
            playerProvider);
    }

    @Override
    public void run() {
        if (server == null || minestomPlayerService == null) {
            throw new IllegalStateException("server not setup, did you forget to call setup()?");
        }
        ServerConfig serverConfig = registry.get(ServerConfig.class)
            .orElseThrow(() -> new IllegalStateException("ServerConfig not loaded"));
        server.start(serverConfig.host(), serverConfig.port());
    }

    @Override
    public EventNode<Event> eventNode() {
        if (server == null) {
            throw new IllegalStateException("server not setup, did you forget to call setup()?");
        }
        return MinecraftServer.getGlobalEventHandler();
    }

    @Override
    public MinestomPlayerService<P> playerService() {
        return minestomPlayerService;
    }
}
