package net.cytonic.cytosis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import eu.koboo.minestom.invue.api.ViewRegistry;
import eu.koboo.minestom.invue.core.MinestomInvue;
import io.github.classgraph.ClassGraph;
import io.github.togar2.pvp.MinestomPvP;
import io.github.togar2.pvp.feature.CombatFeatureSet;
import io.github.togar2.pvp.feature.CombatFeatures;
import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.DatabaseManager;
import net.cytonic.cytosis.data.adapters.InstantAdapter;
import net.cytonic.cytosis.data.adapters.KeyAdapter;
import net.cytonic.cytosis.data.adapters.PreferenceAdapter;
import net.cytonic.cytosis.data.adapters.TypedNamespaceAdapter;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.objects.ServerGroup;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.data.serializers.KeySerializer;
import net.cytonic.cytosis.data.serializers.PosSerializer;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.events.EventListener;
import net.cytonic.cytosis.events.api.Async;
import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.*;
import net.cytonic.cytosis.messaging.nats.NatsManager;
import net.cytonic.cytosis.metrics.CytosisOpenTelemetry;
import net.cytonic.cytosis.metrics.MetricsHooks;
import net.cytonic.cytosis.metrics.MetricsManager;
import net.cytonic.cytosis.nicknames.NicknameManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.CytosisPlayerProvider;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.plugins.loader.PluginClassLoader;
import net.cytonic.cytosis.utils.BlockPlacementUtils;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.cytonic.cytosis.utils.polar.PolarExtension;
import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main class for Cytosis
 */
@Getter
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public final class Cytosis {

    /**
     * the instance ID is used to identify the server
     */
    public static final String SERVER_ID = generateID();
    /**
     * The instance of Gson for serializing and deserializing objects. (Mostly for preferences).
     */
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(TypedNamespace.class, new TypedNamespaceAdapter()).registerTypeAdapter(Preference.class, new PreferenceAdapter<>()).registerTypeAdapter(Key.class, new KeyAdapter()).registerTypeAdapter(Instant.class, new InstantAdapter()).registerTypeAdapterFactory(new TypedNamespaceAdapter()).registerTypeAdapterFactory(new PreferenceAdapter<>()).registerTypeAdapterFactory(new KeyAdapter()).enableComplexMapKeySerialization().setStrictness(Strictness.LENIENT).serializeNulls().create();
    public static final GsonConfigurationLoader.Builder GSON_CONFIGURATION_LOADER = GsonConfigurationLoader.builder()
            .indent(0)
            .defaultOptions(opts -> opts
                    .shouldCopyDefaults(true)
                    .serializers(builder -> {
                        builder.registerAnnotatedObjects(ObjectMapper.factory());
                        builder.register(Key.class, new KeySerializer());
                        builder.register(Pos.class, new PosSerializer());
                    })
            );
    /**
     * The version of Cytosis
     */
    public static final String VERSION = "0.1";
    public static final ViewRegistry VIEW_REGISTRY = MinestomInvue.create();

    public static final boolean IS_NOMAD = System.getenv().containsKey("NOMAD_JOB_ID");
    @Setter
    @Getter
    private static ServerGroup serverGroup = new ServerGroup("cytonic", "lobby", true);
    // manager stuff
    @Getter
    private static MinecraftServer minecraftServer;
    @Getter
    private static net.minestom.server.instance.InstanceManager minestomInstanceManager;
    @Setter
    @Getter
    private static InstanceContainer defaultInstance;
    @Getter
    private static EventHandler eventHandler;
    @Getter
    private static ConnectionManager connectionManager;
    @Getter
    private static CommandManager commandManager;
    @Getter
    private static CommandHandler commandHandler;
    @Getter
    private static FileManager fileManager;
    @Getter
    private static DatabaseManager databaseManager;
    @Getter
    private static ConsoleSender consoleSender;
    @Getter
    private static RankManager rankManager;
    @Getter
    private static ChatManager chatManager;
    @Getter
    private static PlayerListManager playerListManager;
    @Getter
    private static CytonicNetwork cytonicNetwork;
    @Getter
    private static PluginManager pluginManager;
    @Getter
    private static SideboardManager sideboardManager;
    @Getter
    private static NPCManager npcManager;
    @Getter
    private static List<String> flags;
    @Getter
    private static FriendManager friendManager;
    @Getter
    private static PreferenceManager preferenceManager;
    @Getter
    private static VanishManager vanishManager;
    @Getter
    private static NetworkCooldownManager networkCooldownManager;
    @Getter
    private static InstanceManager instanceManager;
    @Getter
    private static ActionbarManager actionbarManager;
    @Getter
    private static NatsManager natsManager;
    @Getter
    private static SnooperManager snooperManager;
    @Getter
    private static MetricsManager metricsManager;
    @Getter
    private static CommandDisablingManager commandDisablingManager;
    @Getter
    private static ServerInstancingManager serverInstancingManager;
    @Getter
    private static NicknameManager nicknameManager;
    @Getter
    @Setter
    private static boolean metricsEnabled = false;


    private Cytosis() {
    }

    /**
     * The entry point for the Minecraft Server
     *
     * @param args Runtime flags
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        flags = List.of(args);
        if (!flags.contains("--no-metrics")) {
            CytosisOpenTelemetry.setup();
        }
        metricsManager = new MetricsManager();
        // handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Logger.error("Uncaught exception in thread " + t.getName(), e);
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        });

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            try {
                Logger.error("Uncaught exception in thread " + t.getName(), e);
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        });

        // Initialize the server
        Logger.info("Starting Cytosis server...");
        minecraftServer = MinecraftServer.init();
        MinecraftServer.getConnectionManager().setPlayerProvider(new CytosisPlayerProvider());
        MinecraftServer.setBrandName("Cytosis");

        MinecraftServer.getBenchmarkManager().enable(Duration.ofSeconds(10L));

        Logger.info("Starting instance managers.");
        minestomInstanceManager = MinecraftServer.getInstanceManager();
        instanceManager = new InstanceManager();

        Logger.info("Starting connection manager.");
        connectionManager = MinecraftServer.getConnectionManager();

        // Commands
        Logger.info("Starting command manager.");
        commandManager = MinecraftServer.getCommandManager();

        Logger.info("Setting console command sender.");
        consoleSender = commandManager.getConsoleSender();

        //chat manager
        Logger.info("Starting chat manager.");
        chatManager = new ChatManager();

        // instances
        Logger.info("Creating instance container");
        defaultInstance = minestomInstanceManager.createInstanceContainer();

        Logger.info("Creating file manager");
        fileManager = new FileManager();

        // Everything after this point depends on config contents
        Logger.info("Initializing file manager");
        fileManager.init();

        Logger.info("Loading Cytosis Settings");
        CytosisSettings.loadEnvironmentVariables();
        CytosisSettings.loadCommandArgs();

        Logger.info("Initializing database");
        databaseManager = new DatabaseManager();
        databaseManager.setupDatabases();

        Logger.info("Starting NATS manager!");
        natsManager = new NatsManager();
        if (!flags.contains("--ci-test")) {
            natsManager.setup(); // don't connect to NATS in compile and run checks
        } else {
            Logger.warn("Skipping NATS manager setup for CI test!");
        }

        Logger.info("Starting Snooper Manager");
        snooperManager = new SnooperManager();
        Logger.info("Loading snooper channels from redis");
        snooperManager.loadChannelsFromRedis();
        Logger.info("Loading Cytosis snoops");
        // load snoops
        snooperManager.registerChannel(CytosisSnoops.PLAYER_BAN);
        snooperManager.registerChannel(CytosisSnoops.PLAYER_UNBAN);
        snooperManager.registerChannel(CytosisSnoops.PLAYER_KICK);
        snooperManager.registerChannel(CytosisSnoops.PLAYER_UNMUTE);
        snooperManager.registerChannel(CytosisSnoops.PLAYER_MUTE);
        snooperManager.registerChannel(CytosisSnoops.PLAYER_WARN);
        snooperManager.registerChannel(CytosisSnoops.SERVER_ERROR);
        snooperManager.registerChannel(CytosisSnoops.CHANGE_RANK);
        snooperManager.registerChannel(CytosisSnoops.PLAYER_NICKNAME);
        snooperManager.registerChannel(CytosisSnoops.PLAYER_SERVER_CHANGE);

        Logger.info("Enabling proxy authentication!");
        VelocityProxy.enable(CytosisSettings.SERVER_SECRET);

        Logger.info("Starting server instancing manager");
        serverInstancingManager = new ServerInstancingManager();

        Logger.info("Initializing block placements");
        BlockPlacementUtils.init();

        Logger.info("Initializing view registry");
        VIEW_REGISTRY.enable();

        Logger.info("Adding a singed command packet handler");
        // commands
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSignedCommandChatPacket.class, (packet, p) -> MinecraftServer.getPacketListenerManager().processClientPacket(new ClientCommandChatPacket(packet.message()), p.getPlayerConnection(), p.getPlayerConnection().getConnectionState()));

        if (metricsEnabled) {
            Logger.info("Starting metric hooks");
            MetricsHooks.init();
        }


        Thread.ofVirtual().name("Cs-WorldLoader").start(Cytosis::loadWorld);

        Logger.info("Initializing server commands");
        commandHandler = new CommandHandler();
        commandHandler.registerCytosisCommands();

        Logger.info("Setting up command disabling");
        commandDisablingManager = new CommandDisablingManager();
        commandDisablingManager.loadRemotes();
        commandDisablingManager.setupConsumers();


        Logger.info("Setting up event handlers");
        eventHandler = new EventHandler(MinecraftServer.getGlobalEventHandler());
        eventHandler.init();

        Logger.info("Loading player preferences");
        preferenceManager = new PreferenceManager();

        Logger.info("Loading vanish manager!");
        vanishManager = new VanishManager();
        Runtime.getRuntime().addShutdownHook(new Thread(Cytosis::shutdownHandler));
        MinecraftServer.getSchedulerManager().buildShutdownTask(Cytosis::shutdownHandler);

        Logger.info("Starting Player list manager");
        playerListManager = new PlayerListManager();

        Logger.info("Starting Nickname manager!");
        nicknameManager = new NicknameManager();

        Logger.info("Starting Friend manager!");
        friendManager = new FriendManager();
        friendManager.init();

        Logger.info("Initializing Rank Manager");
        rankManager = new RankManager();
        rankManager.init();

        Logger.info("Creating sideboard manager!");
        sideboardManager = new SideboardManager();
        sideboardManager.autoUpdateBoards(TaskSchedule.seconds(1L));

        Logger.info("Starting NPC manager!");
        npcManager = new NPCManager();

        try {
            Logger.info("Loading network setup!");
            cytonicNetwork = new CytonicNetwork();
            cytonicNetwork.importData();
            cytonicNetwork.getServers().put(SERVER_ID, new CytonicServer(Utils.getServerIP(), SERVER_ID, CytosisSettings.SERVER_PORT));
        } catch (Exception e) {
            Logger.error("An error occurred whilst loading network setup!", e);
        }

        Logger.info("Starting cooldown managers");
        networkCooldownManager = new NetworkCooldownManager(databaseManager.getRedisDatabase());
        networkCooldownManager.importFromRedis();
        Logger.info("Started network cooldown manager");

        Logger.info("starting actionbar manager");
        actionbarManager = new ActionbarManager();
        actionbarManager.init();

        Logger.info("Loading PVP");
        MinestomPvP.init();
        CombatFeatureSet modernVanilla = CombatFeatures.modernVanilla();
        MinecraftServer.getGlobalEventHandler().addChild(modernVanilla.createNode());
        MinecraftServer.getConnectionManager().setPlayerProvider(CytosisPlayer::new);


        //
        // PLUGIN LOADING IS ALWAYS LAST!!!!
        // (This is so any apis it depends on are guarenteed to already by loaded!)
        //
        Logger.info("Initializing Plugin Manager!");
        pluginManager = new PluginManager();
        Logger.info("Loading plugins!");
        try {
            if (new File("plugins").exists() && new File("plugins").isDirectory()) {
                pluginManager.loadPlugins(Path.of("plugins"));
            } else {
                new File("plugins").mkdir();
                Logger.info("Created plugins directory!");
            }
        } catch (Exception e) {
            Logger.error("An error occurred whilst loading plugins!", e);
            throw new RuntimeException("An error occurred whilst loading plugins!", e);
        }

        long start2 = System.currentTimeMillis();
        Logger.info("Scanning for listeners in plugins!");

        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(Cytosis.class.getClassLoader());
        loaders.addAll(PluginClassLoader.loaders);

        ClassGraph graph = new ClassGraph()
                .acceptPackages("net.cytonic") // skip dependencies
                .enableAllInfo()
                .overrideClassLoaders(loaders.toArray(new ClassLoader[0]));

        AtomicInteger counter = new AtomicInteger(0);
        graph.scan()
                .getClassesWithMethodAnnotation(Listener.class.getName())
                .forEach(classInfo -> {
                    Class<?> clazz = classInfo.loadClass();

                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Listener.class)) {
                            int priority = method.isAnnotationPresent(Priority.class) ? method.getAnnotation(Priority.class).value() : 50;
                            boolean async = method.isAnnotationPresent(Async.class);

                            Object instance;
                            try {
                                instance = clazz.getDeclaredConstructor().newInstance();
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException e) {
                                Logger.error("The class " + clazz.getSimpleName() + " needs to have a public, no argument constructor to have an @Listener in it!", e);
                                return;
                            }


                            Class<? extends Event> eventClass;
                            try {
                                eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                            } catch (ClassCastException e) {
                                Logger.error("The parameter of a method annotated with @Listener must be a valid event!", e);
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
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    Logger.error("Failed to call @Listener!", e);
                                }
                            }
                            ));
                        }
                    }
                });
        Logger.info("Finished scanning for listeners in plugins in " + (System.currentTimeMillis() - start2) + "ms!");

        // Start the server
        Logger.info("Server started on port " + CytosisSettings.SERVER_PORT);
        minecraftServer.start("0.0.0.0", CytosisSettings.SERVER_PORT);
        MinecraftServer.getExceptionManager().setExceptionHandler(e -> Logger.error("Uncaught exception: ", e));

        natsManager.sendStartup();

        long end = System.currentTimeMillis();
        Logger.info("Server started in " + (end - start) + "ms!");
        Logger.info("Server id = " + SERVER_ID);

        if (flags.contains("--ci-test")) {
            Logger.info("Stopping server due to '--ci-test' flag.");
            MinecraftServer.stopCleanly();
        }
    }

    /**
     * Gets the players currently on THIS instance
     *
     * @return a set of players
     */
    // every object the server makes is a CytosisPlayer -- or decentdant from one
    public static Set<CytosisPlayer> getOnlinePlayers() {
        HashSet<CytosisPlayer> players = new HashSet<>();

        for (@NotNull Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            try {
                CytosisPlayer cp = (CytosisPlayer) onlinePlayer;
                players.add(cp);
            } catch (ClassCastException e) {
                // ignored
            }
        }
        return players;
    }

    /**
     * Gets the player if they are on THIS instance, by USERNAME
     *
     * @param username The name to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static Optional<CytosisPlayer> getPlayer(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable((CytosisPlayer) MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(username));
    }

    /**
     * Gets the player if they are on THIS instance, by UUID
     *
     * @param uuid The uuid to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static Optional<CytosisPlayer> getPlayer(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable((CytosisPlayer) MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid));
    }

    /**
     * Loads the world based on the settings
     */
    public static void loadWorld() {
        if (CytosisSettings.SERVER_WORLD_NAME.isEmpty()) {
            Logger.info("Generating basic world");
            defaultInstance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.WHITE_STAINED_GLASS));
            defaultInstance.setChunkSupplier(LightingChunk::new);
            Logger.info("Basic world loaded!");
            return;
        }
        Logger.info("Loading world '" + CytosisSettings.SERVER_WORLD_NAME + "'");
        databaseManager.getMysqlDatabase().getWorld(CytosisSettings.SERVER_WORLD_NAME).whenComplete((polarWorld, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst initializing the world! Reverting to a basic world", throwable);
                defaultInstance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.WHITE_STAINED_GLASS));
                defaultInstance.setChunkSupplier(LightingChunk::new);
            } else {
                defaultInstance.setChunkLoader(new PolarLoader(polarWorld).setWorldAccess(new PolarExtension()));
                defaultInstance.setChunkSupplier(LightingChunk::new);
                defaultInstance.enableAutoChunkLoad(true);
                Logger.info("World loaded!");
            }
        });
    }

    /**
     * Generates a random Server ID:
     * <p>
     * TODO: make a check for existing server ids
     *
     * @return a random Server ID
     */
    private static String generateID() {
        //todo: make a check for existing server ids
        StringBuilder id = new StringBuilder();
        Random random = new Random();
        id.append((char) (random.nextInt(26) + 'a'));
        for (int i = 0; i < 4; i++) {
            id.append(random.nextInt(10));
        }
        id.append((char) (random.nextInt(26) + 'a'));
        return id.toString();
    }

    /**
     * Gets the Raw ID of the server
     * <p>
     * For example, Cytosis-a1234b would return a1234b
     *
     * @return The raw ID
     */
    public static String getRawID() {
        return Cytosis.SERVER_ID.replace("Cytosis-", "");
    }

    private static void shutdownHandler() {
        natsManager.shutdown();
        databaseManager.shutdown();
        sideboardManager.cancelUpdates();
        pluginManager.unloadPlugins();
        getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.kick(Msg.mm("<red>The server is shutting down.")));
    }

    public static CytonicServer currentServer() {
        return new CytonicServer(Utils.getServerIP(), SERVER_ID, CytosisSettings.SERVER_PORT);
    }
}