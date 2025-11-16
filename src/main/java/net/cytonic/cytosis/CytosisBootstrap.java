package net.cytonic.cytosis;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import me.devnatan.AnvilInputFeature;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewFrame;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;

import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.metrics.MetricsHooks;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;
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
        initViewFrame();

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

    private void initViewFrame() {
        Logger.info("Initializing view frame");
        ViewFrame viewFrame = ViewFrame.create();

        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Cytosis.class.getClassLoader());
        loaders.addAll(PluginClassLoader.LOADERS);

        ClassGraph graph = new ClassGraph()
            .acceptPackages("net.cytonic")
            .enableAllInfo()
            .overrideClassLoaders(loaders.toArray(new ClassLoader[0]));

        try (ScanResult result = graph.scan()) {
            result.getSubclasses(View.class).loadClasses().forEach(foundClass -> {
                try {
                    if (!foundClass.getPackage().getName().startsWith("net.cytonic")) {
                        return;
                    }

                    Constructor<?> constructor = foundClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    View instance = (View) constructor.newInstance();
                    viewFrame.with(instance);
                } catch (Exception e) {
                    Logger.error("An error occurred whilst loading views!", e);
                }
            });
        } catch (Exception e) {
            Logger.error("An error occurred whilst loading views!", e);
        }

        viewFrame.install(AnvilInputFeature.AnvilInput);
        cytosisContext.registerComponent(viewFrame.register());
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
    }

    /**
     * Initializes and configures the Minestom server components required for the application.
     */
    private void initMinestom() {
        cytosisContext.registerComponent(MinecraftServer.init(new Auth.Velocity(cytosisContext.getComponent(
            CytosisSettings.class).getServerConfig().getSecret())));
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
                .processClientPacket(new ClientCommandChatPacket(packet.message()), p.getPlayerConnection()));

        Thread.ofVirtual().name("Cytosis-WorldLoader").start(Cytosis::loadWorld);
    }

    private void startServer() {
        int port = cytosisContext.getComponent(CytosisSettings.class).getServerConfig().getPort();
        Logger.info("Server started on port " + port);
        cytosisContext.getComponent(MinecraftServer.class).start("0.0.0.0", port);
        MinecraftServer.getExceptionManager().setExceptionHandler(e -> Logger.error("Uncaught exception: ", e));
    }
}