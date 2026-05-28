package net.cytonic.cytosis;

import java.io.IOException;
import java.lang.reflect.Constructor;

import me.devnatan.AnvilInputFeature;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewFrame;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.CommandDisablingManager;
import net.cytonic.cytosis.metrics.MetricsHooks;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.server.AbstractCytosisServer;
import net.cytonic.cytosis.utils.BlockPlacementUtils;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.cytonic.protocol.utils.IndexHolder;

/**
 * Main bootstrap class responsible for initializing and starting the Cytosis server. This class orchestrates the entire
 * server start-up process, including configuration, component registration, server initialization, and runtime
 * settings.
 */
public class CytosisBootstrap {

    private final CytosisContext cytosisContext;
    private final AbstractCytosisServer<? extends CytosisPlayer> server;

    public CytosisBootstrap(AbstractCytosisServer<? extends CytosisPlayer> server, CytosisContext context) {
        this.server = server;
        this.cytosisContext = context;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        Logger.info("Starting Cytosis server");

        cytosisContext.registerComponent(server.getConfigOrThrow(CytosisConfig.class));
        cytosisContext.registerComponent(AbstractCytosisServer.class, server);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Logger.error("Uncaught exception in thread " + t.getName(), e);
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        });

        Logger.info("Loading indexes");
        try {
            IndexHolder.initialize();
        } catch (IOException e) {
            Logger.error("Failed to initialize indexes: ", e);
            System.exit(122);
            return;
        }

        initMinestom();
        try {
            BootstrapRegistrationUtils.registerCytosisComponents(cytosisContext);
        } catch (Exception ex) {
            Logger.error("failed to register components!", ex);
        }

        // register commands after every component is registered to avoid missing dependencies
        cytosisContext.getComponent(CommandHandler.class).registerCytosisCommands();
        //This has to load after command registration
        cytosisContext.getComponent(CommandDisablingManager.class).loadRemotes();
        initWorld();

        if (cytosisContext.isMetricsEnabled()) {
            Logger.info("Starting metric hooks");
            MetricsHooks.init();
        }

        ShutdownHandler.init();
//        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        try {
            BootstrapRegistrationUtils.registerListeners(cytosisContext);
        } catch (Exception ex) {
            Logger.error("Failed to register components!", ex);
        }

        initViewFrame();
        Cytosis.get(EventHandler.class).init();

        startServer();
        long end = System.currentTimeMillis();
        Logger.info("Server started in " + (end - startTime) + "ms!");
        Logger.info("Server id = " + Cytosis.CONTEXT.SERVER_ID);
    }

    private void shutdown() {
        if (cytosisContext.isStopping()) return;

        Cytosis.CONTEXT.setStopping(true);
        Logger.info("Shutdown signal received!");

        if (Cytosis.getOnlinePlayers().isEmpty()) {
//            Cytosis.CONTEXT.shutdownHandler();
            MinecraftServer.stopCleanly();
            return;
        }
        if (Cytosis.get(Environment.class) == Environment.DEVELOPMENT) {
            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                player.kickInternal(Msg.red("Dev server shutting down"));
            }
            return;
        }

        System.out.println("SLOW SHUTDOWN");

//        Cytosis.CONTEXT.shutdownHandler();
        MinecraftServer.stopCleanly();
    }

    private void initViewFrame() {
        Logger.info("Initializing view frame");
        ViewFrame viewFrame = ViewFrame.create();

        IndexView index = IndexHolder.get();

        index.getAllKnownSubclasses(View.class).stream()
            .filter(ci -> ci.name().startsWith(DotName.createSimple("net.cytonic")))
            .forEach(ci -> {
                try {
                    Class<?> clazz = Utils.loadClass(ci.name().toString());
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    View instance = (View) constructor.newInstance();
                    viewFrame.with(instance);
                } catch (Exception e) {
                    Logger.error("An error occurred whilst loading menu views!", e);
                }
            });

        viewFrame.install(AnvilInputFeature.AnvilInput);
        cytosisContext.registerComponent(viewFrame.register());
    }

    /**
     * Initializes and configures the Minestom server components required for the application.
     */
    private void initMinestom() {
        MinecraftServer.setBrandName("Cytosis");
//        MinecraftServer.getBenchmarkManager().enable(Duration.ofSeconds(10L));

        Logger.info("Starting instance managers.");
        cytosisContext.registerComponent(MinecraftServer.getInstanceManager());
        Logger.info("Starting connection manager.");
        cytosisContext.registerComponent(MinecraftServer.getConnectionManager());
        Logger.info("Starting command manager.");
        CommandManager commandManager = cytosisContext.registerComponent(MinecraftServer.getCommandManager());
        Logger.info("Setting console command sender.");
        cytosisContext.registerComponent(commandManager.getConsoleSender());
    }

    /**
     * Initializes the world by setting up the necessary components and configurations required for the world to run
     * properly.
     */
    private void initWorld() {
        Logger.info("Initializing block placements");
        BlockPlacementUtils.init();
        Logger.info("Adding a singed command packet handler");
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSignedCommandChatPacket.class,
            (packet, p) -> MinecraftServer.getPacketListenerManager()
                .processClientPacket(new ClientCommandChatPacket(packet.message()), p.getPlayerConnection()));
    }

    private void startServer() {
//        int port = cytosisContext.getComponent(CytosisSettings.class).getServerConfig().getPort();
//        Logger.info("Server started on port " + port);
//        cytosisContext.getComponent(MinecraftServer.class).start("0.0.0.0", port);
        MinecraftServer.getExceptionManager().setExceptionHandler(e -> Logger.error("Uncaught exception: ", e));
    }

}