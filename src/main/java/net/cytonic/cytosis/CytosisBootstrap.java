package net.cytonic.cytosis;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import me.devnatan.AnvilInputFeature;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewFrame;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.title.Title;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.CommandDisablingManager;
import net.cytonic.cytosis.metrics.MetricsHooks;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.utils.BlockPlacementUtils;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.cytonic.protocol.utils.IndexHolder;

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
        System.setProperty("minestom.shutdown-on-signal", "false");

        Logger.info("Loading indexes");
        try {
            IndexHolder.initialize(List.of(Objects.requireNonNull(PluginManager.PLUGINS_DIR.toFile().listFiles())));
        } catch (IOException e) {
            Logger.error("Failed to initialize indexes: ", e);
            System.exit(122);
            return;
        }

        applySystemSettings();
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

        Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().unstarted(() -> {
            Cytosis.CONTEXT.setStopping(true);
            Logger.info("Shutdown signal received!");

            if (!Cytosis.CONTEXT.isSlowShutdown() || Cytosis.getOnlinePlayers().isEmpty()) {
                Cytosis.CONTEXT.shutdownHandler();
                MinecraftServer.stopCleanly();
                return;
            }

            int seconds = Cytosis.get(CytosisSettings.class).getServerConfig().getShutdownDuration();
            Instant shutdown = Instant.now().plusSeconds(seconds);
            Cytosis.CONTEXT.setShutdownAt(shutdown);
            Logger.info("Shutting server down in %d seconds.", seconds);
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                Cytosis.CONTEXT.shutdownHandler();
                MinecraftServer.stopCleanly();
            }).delay(Duration.ofSeconds(seconds)).schedule();

            BossBar bb = BossBar.bossBar(Msg.red("<b>Server Shutdown</b><white> %s",
                DurationParser.unparse(shutdown, " ")), 1F, Color.RED, BossBar.Overlay.PROGRESS);
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                    bb.progress(Instant.now().until(shutdown, ChronoUnit.SECONDS) / (float) seconds);
                    bb.name(Msg.red("<b>Server Shutdown</b><white> %s", DurationParser.unparse(shutdown, " ")));
                }).repeat(Duration.ofSeconds(1))
                .schedule();
            Cytosis.getOnlinePlayers().forEach(player -> {
                player.showBossBar(bb);
                player.showTitle(Title.title(Msg.redSplash("SERVER SHUTTING DOWN", ""),
                    Msg.mm("This server will shut down in %s",
                        DurationParser.unparse(shutdown, " ")), 10, 40, 10));
            });

            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException e) {
                System.err.println("Interrupted!");
                Thread.currentThread().interrupt();
            }
        }));

        try {
            BootstrapRegistrationUtils.registerListeners(cytosisContext);
        } catch (Exception ex) {
            Logger.error("Failed to register components!", ex);
        }

        Cytosis.get(PluginManager.class).initializePlugins();
        initViewFrame();
        Cytosis.get(EventHandler.class).init();

        startServer();
        long end = System.currentTimeMillis();
        Logger.info("Server started in " + (end - startTime) + "ms!");
        Logger.info("Server id = " + Cytosis.CONTEXT.SERVER_ID);

        if (cytosisContext.getFlags().contains("--ci-test")) {
            Logger.info("Stopping server due to '--ci-test' flag.");
            MinecraftServer.stopCleanly();
        }
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
     * Applies system settings and loads environment.
     */
    private void applySystemSettings() {
        Logger.info("Creating file manager");
        Logger.info("Initializing file manager");
        cytosisContext.registerComponent(new FileManager());
        Logger.info("Loading Cytosis Settings");
    }

    /**
     * Initializes and configures the Minestom server components required for the application.
     */
    private void initMinestom() {
        cytosisContext.registerComponent(MinecraftServer.init(
            new Auth.Velocity(cytosisContext.getComponent(CytosisSettings.class).getServerConfig().getSecret())));
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
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSignedCommandChatPacket.class,
            (packet, p) -> MinecraftServer.getPacketListenerManager()
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