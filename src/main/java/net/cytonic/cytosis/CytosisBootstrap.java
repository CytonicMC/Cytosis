package net.cytonic.cytosis;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.classgraph.ClassGraph;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.Event;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;

import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.MysqlDatabase;
import net.cytonic.cytosis.data.RedisDatabase;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.events.EventListener;
import net.cytonic.cytosis.events.api.Async;
import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.ActionbarManager;
import net.cytonic.cytosis.managers.ChatManager;
import net.cytonic.cytosis.managers.CommandDisablingManager;
import net.cytonic.cytosis.managers.FriendManager;
import net.cytonic.cytosis.managers.InstanceManager;
import net.cytonic.cytosis.managers.LocalCooldownManager;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.cytosis.managers.NpcManager;
import net.cytonic.cytosis.managers.PlayerListManager;
import net.cytonic.cytosis.managers.PreferenceManager;
import net.cytonic.cytosis.managers.RankManager;
import net.cytonic.cytosis.managers.ServerInstancingManager;
import net.cytonic.cytosis.managers.SideboardManager;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.managers.VanishManager;
import net.cytonic.cytosis.messaging.NatsManager;
import net.cytonic.cytosis.metrics.CytosisOpenTelemetry;
import net.cytonic.cytosis.metrics.MetricsHooks;
import net.cytonic.cytosis.metrics.MetricsManager;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;
import net.cytonic.cytosis.utils.BlockPlacementUtils;

public class CytosisBootstrap {

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
        setupLoggingAndCrashHandling();

        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        setupMetricsEarly();
        initFilesAndConfig();
        initMinestom();
        initDatabase();
        initCoreManagers();
        loadNetworkTopology();
        initMessagingAndSnooper();
        initServerInstancingAndBlocks();
        initViewAndPacketsAndMetricsHooks();

        Thread.ofVirtual().name("Cytosis-WorldLoader").start(Cytosis::loadWorld);

        initCommandsAndDisabling();
        wireShutdownHandlers();
        initPlayerFacingManagers();
        initCooldowns();
        initActionbar();
        loadPluginsAndScanListeners();
        startServer();

        long end = System.currentTimeMillis();
        Logger.info("Server started in " + (end - startTime) + "ms!");
        Logger.info("Server id = " + CytosisContext.SERVER_ID);

        if (cytosisContext.getFlags().contains("--ci-test")) {
            Logger.info("Stopping server due to '--ci-test' flag.");
            MinecraftServer.stopCleanly();
        }
    }

    private void initDatabase() {
        Logger.info("Initializing database");
        cytosisContext.registerComponent(new MysqlDatabase());
        cytosisContext.registerComponent(new RedisDatabase());
    }

    private void setupLoggingAndCrashHandling() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Logger.error("Uncaught exception in thread " + t.getName(), e);
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        });
    }

    private void setupMetricsEarly() {
        if (!cytosisContext.getFlags().contains("--no-metrics")) {
            CytosisOpenTelemetry.setup();
        }

        cytosisContext.registerComponent(new MetricsManager());
    }

    private void initFilesAndConfig() {
        Logger.info("Creating file manager");
        Logger.info("Initializing file manager");
        cytosisContext.registerComponent(new FileManager());

        Logger.info("Loading Cytosis Settings");
        CytosisSettings.loadEnvironmentVariables();
        CytosisSettings.loadCommandArgs();
    }

    private void initMinestom() {
        cytosisContext.registerComponent(MinecraftServer.init(new Auth.Velocity(CytosisSettings.SERVER_SECRET)));
        MinecraftServer.getConnectionManager().setPlayerProvider(CytosisPlayer::new);
        MinecraftServer.setBrandName("Cytosis");
        MinecraftServer.getBenchmarkManager().enable(Duration.ofSeconds(10L));
    }

    private void initCoreManagers() {
        Logger.info("Setting up event handlers");
        cytosisContext.registerComponent(new EventHandler(MinecraftServer.getGlobalEventHandler()));

        Logger.info("Loading player preferences");
        cytosisContext.registerComponent(new PreferenceManager());

        Logger.info("Loading vanish manager!");
        cytosisContext.registerComponent(new VanishManager());

        Logger.info("Starting instance managers.");
        net.minestom.server.instance.InstanceManager minestomInstanceManager = cytosisContext.registerComponent(
            MinecraftServer.getInstanceManager());
        cytosisContext.registerComponent(new InstanceManager());

        Logger.info("Starting connection manager.");
        cytosisContext.registerComponent(MinecraftServer.getConnectionManager());

        Logger.info("Starting command manager.");
        CommandManager commandManager = cytosisContext.registerComponent(MinecraftServer.getCommandManager());

        Logger.info("Setting console command sender.");
        cytosisContext.registerComponent(commandManager.getConsoleSender());

        Logger.info("Starting chat manager.");
        cytosisContext.registerComponent(new ChatManager());

        Logger.info("Creating instance container");
        cytosisContext.registerComponent(minestomInstanceManager.createInstanceContainer());
    }

    private void initMessagingAndSnooper() {
        Logger.info("Starting NATS manager!");
        NatsManager natsManager = cytosisContext.registerComponent(new NatsManager());
        if (!cytosisContext.getFlags().contains("--ci-test")) {
            natsManager.setup();
            // Ensure servers are fetched early during bootstrap once NATS is initialized
            natsManager.fetchServers();
        } else {
            Logger.warn("Skipping NATS manager setup for CI test!");
        }

        Logger.info("Starting Snooper Manager");
        SnooperManager snooperManager = cytosisContext.registerComponent(new SnooperManager());
    }

    private void initServerInstancingAndBlocks() {
        Logger.info("Starting server instancing manager");
        cytosisContext.registerComponent(new ServerInstancingManager());

        Logger.info("Initializing block placements");
        BlockPlacementUtils.init();
    }

    private void initViewAndPacketsAndMetricsHooks() {
        Logger.info("Initializing view registry");
        Cytosis.VIEW_REGISTRY.enable();

        Logger.info("Adding a singed command packet handler");
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSignedCommandChatPacket.class, (packet, p) ->
            MinecraftServer.getPacketListenerManager()
                .processClientPacket(new ClientCommandChatPacket(packet.message()), p.getPlayerConnection(),
                    p.getPlayerConnection().getConnectionState()));

        if (cytosisContext.isMetricsEnabled()) {
            Logger.info("Starting metric hooks");
            MetricsHooks.init();
        }
    }

    private void initCommandsAndDisabling() {
        Logger.info("Initializing server commands");
        cytosisContext.registerComponent(new CommandHandler());

        Logger.info("Setting up command disabling");
        cytosisContext.registerComponent(new CommandDisablingManager());
    }

    private void initEventsAndPreferencesAndVanish() {
        Logger.info("Setting up event handlers");
        cytosisContext.registerComponent(new EventHandler(MinecraftServer.getGlobalEventHandler()));

        Logger.info("Loading player preferences");
        cytosisContext.registerComponent(new PreferenceManager());

        Logger.info("Loading vanish manager!");
        cytosisContext.registerComponent(new VanishManager());
    }

    private void wireShutdownHandlers() {
        // TODO: do only one of those , both are executing otherwise?
        Runtime.getRuntime().addShutdownHook(new Thread(cytosisContext::shutdownHandler));
        MinecraftServer.getSchedulerManager().buildShutdownTask(cytosisContext::shutdownHandler);
    }

    private void initPlayerFacingManagers() {
        Logger.info("Starting Player list manager");
        cytosisContext.registerComponent(new PlayerListManager());

        Logger.info("Starting Nickname manager!");
        cytosisContext.registerComponent(new NicknameManager());

        Logger.info("Starting Friend manager!");
        cytosisContext.registerComponent(new FriendManager());

        Logger.info("Initializing Rank Manager");
        cytosisContext.registerComponent(new RankManager());

        Logger.info("Creating sideboard manager!");
        cytosisContext.registerComponent(new SideboardManager());

        Logger.info("Starting NPC manager!");
        cytosisContext.registerComponent(new NpcManager());
    }

    private void loadNetworkTopology() {
        try {
            Logger.info("Loading network setup!");
            CytonicNetwork cytonicNetwork = cytosisContext.registerComponent(new CytonicNetwork());
        } catch (Exception e) {
            Logger.error("An error occurred whilst loading network setup!", e);
        }
    }

    private void initCooldowns() {
        Logger.info("Starting cooldown managers");
        cytosisContext.registerComponent(new LocalCooldownManager());
        cytosisContext.registerComponent(new NetworkCooldownManager(cytosisContext.getComponent(RedisDatabase.class)));
    }

    private void initActionbar() {
        Logger.info("Starting actionbar manager");
        cytosisContext.registerComponent(new ActionbarManager());
    }

    private void loadPluginsAndScanListeners() {
        Logger.info("Initializing Plugin Manager!");
        cytosisContext.registerComponent(new PluginManager());
        Logger.info("Loading plugins!");

        long start2 = System.currentTimeMillis();
        Logger.info("Scanning for listeners in plugins!");

        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Cytosis.class.getClassLoader());
        loaders.addAll(PluginClassLoader.LOADERS);

        ClassGraph graph = new ClassGraph()
            .acceptPackages("net.cytonic")
            .enableAllInfo()
            .overrideClassLoaders(loaders.toArray(new ClassLoader[0]));

        AtomicInteger counter = new AtomicInteger(0);
        EventHandler eventHandler = cytosisContext.getComponent(EventHandler.class);
        graph.scan()
            .getClassesWithMethodAnnotation(Listener.class.getName())
            .forEach(classInfo -> {
                Class<?> clazz = classInfo.loadClass();
                Object instance;
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    instance = constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    Logger.error("The class " + clazz.getSimpleName()
                        + " needs to have a public, no argument constructor to have an @Listener in it!", e);
                    return;
                }

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Listener.class)) {
                        method.setAccessible(true);
                        int priority =
                            method.isAnnotationPresent(Priority.class) ? method.getAnnotation(Priority.class).value()
                                : 50;
                        boolean async = method.isAnnotationPresent(Async.class);

                        Class<? extends Event> eventClass;
                        try {
                            eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                        } catch (ClassCastException e) {
                            Logger.error("The parameter of a method annotated with @Listener must be a valid event!",
                                e);
                            return;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            Logger.error("Methods annotated with @Listener must have a valid event as a parameter!", e);
                            return;
                        }
                        eventHandler.registerListener(new EventListener<>(
                            "cytosis:annotation-listener-" + counter.getAndIncrement(),
                            async, priority, (Class<Event>) eventClass, event -> {
                            try {
                                method.invoke(instance, event);
                            } catch (IllegalAccessException e) {
                                Logger.error("Failed to call @Listener!", e);
                            } catch (InvocationTargetException e) {
                                Throwable cause = e.getCause();
                                if (cause != null) {
                                    Logger.error("Exception in @Listener method: ", cause);
                                } else {
                                    Logger.error("Unknown error in @Listener method.", e);
                                }
                            }
                        }
                        ));
                    }
                }
            });
        Logger.info("Finished scanning for listeners in plugins in " + (System.currentTimeMillis() - start2) + "ms!");

        eventHandler.init();
    }

    private void startServer() {
        Logger.info("Server started on port " + CytosisSettings.SERVER_PORT);
        cytosisContext.getComponent(MinecraftServer.class).start("0.0.0.0", CytosisSettings.SERVER_PORT);
        MinecraftServer.getExceptionManager().setExceptionHandler(e -> Logger.error("Uncaught exception: ", e));

        cytosisContext.getComponent(NatsManager.class).sendStartup();
    }
}