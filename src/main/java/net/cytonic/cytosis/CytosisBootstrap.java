package net.cytonic.cytosis;

import java.time.Duration;
import java.util.List;

import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;

import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.metrics.MetricsHooks;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.BlockPlacementUtils;

/**
 * Main bootstrap class responsible for initializing and starting the Cytosis server. This class orchestrates the entire
 * server start-up process, including configuration, component registration, server initialization, and runtime
 * settings.
 */
public class CytosisBootstrap {

    public static final String SCAN_PACKAGE_ROOT = "net.cytonic";
    private final CytosisContext cytosisContext;
    private final List<String> argList;

    public CytosisBootstrap(String[] args, CytosisContext context) {
        this.argList = List.of(args);
        this.cytosisContext = context;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        Logger.info("Starting Cytosis server...");

        cytosisContext.setFlags(argList);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Logger.error("Uncaught exception in thread " + t.getName(), e);
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        });

        applySystemSettings();
        initMinestom();
        BootstrapRegistrationUtils.registerCytosisComponents(cytosisContext);
        // register commands after every component is registered to avoid missing dependencies
        cytosisContext.getComponent(CommandHandler.class).registerCytosisCommands();
        initWorld();

        Logger.info("Initializing view registry");
        Cytosis.VIEW_REGISTRY.enable();

        if (cytosisContext.isMetricsEnabled()) {
            Logger.info("Starting metric hooks");
            MetricsHooks.init();
        }

        // TODO: do only one of those , both are executing otherwise?
        Runtime.getRuntime().addShutdownHook(new Thread(cytosisContext::shutdownHandler));
        MinecraftServer.getSchedulerManager().buildShutdownTask(cytosisContext::shutdownHandler);

        BootstrapRegistrationUtils.registerListeners(cytosisContext);
        cytosisContext.getComponent(EventHandler.class).init();

        startServer();

        long end = System.currentTimeMillis();
        Logger.info("Server started in " + (end - startTime) + "ms!");
        Logger.info("Server id = " + CytosisContext.SERVER_ID);

        if (cytosisContext.getFlags().contains("--ci-test")) {
            Logger.info("Stopping server due to '--ci-test' flag.");
            MinecraftServer.stopCleanly();
        }
    }

    /**
     * Applies system settings and loads environment.
     */
    private void applySystemSettings() {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
        Logger.info("Creating file manager");
        Logger.info("Initializing file manager");
        cytosisContext.registerComponent(new FileManager());
        Logger.info("Loading Cytosis Settings");
        CytosisSettings.loadEnvironmentVariables();
        CytosisSettings.loadCommandArgs();
    }

    /**
     * Initializes and configures the Minestom server components required for the application.
     */
    private void initMinestom() {
        cytosisContext.registerComponent(MinecraftServer.init(new Auth.Velocity(CytosisSettings.SERVER_SECRET)));
        MinecraftServer.getConnectionManager().setPlayerProvider(CytosisPlayer::new);
        MinecraftServer.setBrandName("Cytosis");
        MinecraftServer.getBenchmarkManager().enable(Duration.ofSeconds(10L));

        Logger.info("Starting instance managers.");
        InstanceManager minestomInstanceManager = cytosisContext.registerComponent(
            MinecraftServer.getInstanceManager());
        Logger.info("Starting connection manager.");
        cytosisContext.registerComponent(MinecraftServer.getConnectionManager());
        Logger.info("Starting command manager.");
        CommandManager commandManager = cytosisContext.registerComponent(MinecraftServer.getCommandManager());
        Logger.info("Setting console command sender.");
        cytosisContext.registerComponent(commandManager.getConsoleSender());
        Logger.info("Creating instance container");
        cytosisContext.registerComponent(minestomInstanceManager.createInstanceContainer());
    }

    /**
     * Initializes the world by setting up the necessary components and configurations required for the world to run
     * properly.
     */
    private void initWorld() {
        Logger.info("Initializing block placements");
        BlockPlacementUtils.init();
        Logger.info("Adding a singed command packet handler");
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSignedCommandChatPacket.class, (packet, p) ->
            MinecraftServer.getPacketListenerManager()
                .processClientPacket(new ClientCommandChatPacket(packet.message()), p.getPlayerConnection(),
                    p.getPlayerConnection().getConnectionState()));

        Thread.ofVirtual().name("Cytosis-WorldLoader").start(Cytosis::loadWorld);
    }

    private void startServer() {
        Logger.info("Server started on port " + CytosisSettings.SERVER_PORT);
        cytosisContext.getComponent(MinecraftServer.class).start("0.0.0.0", CytosisSettings.SERVER_PORT);
        MinecraftServer.getExceptionManager().setExceptionHandler(e -> Logger.error("Uncaught exception: ", e));
    }
}