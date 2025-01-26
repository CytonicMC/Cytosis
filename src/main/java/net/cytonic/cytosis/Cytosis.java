package net.cytonic.cytosis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.commands.CommandHandler;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.data.DatabaseManager;
import net.cytonic.cytosis.data.adapters.InstantAdapter;
import net.cytonic.cytosis.data.adapters.PreferenceAdapter;
import net.cytonic.cytosis.data.adapters.TypedNamespaceAdapter;
import net.cytonic.cytosis.data.objects.CytonicServer;
import net.cytonic.cytosis.data.objects.ServerGroup;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.events.ServerEventListeners;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.*;
import net.cytonic.cytosis.menus.ClickableItemRegistry;
import net.cytonic.cytosis.messaging.MessagingManager;
import net.cytonic.cytosis.messaging.nats.NatsManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.CytosisPlayerProvider;
import net.cytonic.cytosis.plugins.PluginManager;
import net.cytonic.cytosis.utils.BlockPlacementUtils;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/**
 * The main class for Cytosis
 */
@Getter
@SuppressWarnings("unused")
public final class Cytosis {

    /**
     * the instance ID is used to identify the server
     */
    public static final String SERVER_ID = generateID();
    /**
     * The instance of Gson for serializing and deserializing objects. (Mostly for preferences).
     */
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(TypedNamespace.class, new TypedNamespaceAdapter())
            .registerTypeAdapter(Preference.class, new PreferenceAdapter<>())
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .registerTypeAdapterFactory(new TypedNamespaceAdapter())
            .registerTypeAdapterFactory(new PreferenceAdapter<>())
            .enableComplexMapKeySerialization()
            .setStrictness(Strictness.LENIENT)
            .serializeNulls()
            .create();
    /**
     * The version of Cytosis
     */
    public static final String VERSION = "0.1";
    @Setter
    @Getter
    private static ServerGroup serverGroup = new ServerGroup("default", "default", true);
    // manager stuff
    @Getter
    private static MinecraftServer minecraftServer;
    @Getter
    private static net.minestom.server.instance.InstanceManager minestomInstanceManager;
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
    private static MessagingManager messagingManager;
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


    private Cytosis() {
    }

    /**
     * The entry point for the Minecraft Server
     *
     * @param args Runtime flags
     */
    public static void main(String[] args) {
        // handle uncaught exceptions
        Logger.info("Starting server!");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logger.error("Uncaught exception in thread " + t.getName() + "", e));

        flags = List.of(args);
        long start = System.currentTimeMillis();
        // Initialize the server
        Logger.info("Starting server.");
        minecraftServer = MinecraftServer.init();
        MinecraftServer.getConnectionManager().setPlayerProvider(new CytosisPlayerProvider());
        MinecraftServer.setBrandName("Cytosis");

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
        fileManager.init().whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst initializing the file manager!", throwable);
            } else {
                Logger.info("File manager initialized!");
                CytosisSettings.loadEnvironmentVariables();
                CytosisSettings.loadCommandArgs();
                if (CytosisSettings.SERVER_PROXY_MODE) {
                    Logger.info("Enabling velocity!");
                    VelocityProxy.enable(CytosisSettings.SERVER_SECRET);
                } else mojangAuth();
                Logger.info("Completing nonessential startup tasks.");

                try {
                    completeNonEssentialTasks(start);
                } catch (Exception e) {
                    Logger.error("ERR: ", e);
                }
            }
        });
    }

    /**
     * Gets the players currently on THIS instance
     *
     * @return a set of players
     */
    // every object the server makes is a CytosisPlayer
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
     * Sets up Mojang Authentication
     */
    public static void mojangAuth() {
        Logger.info("Initializing Mojang Authentication");
        MojangAuth.init(); //VERY IMPORTANT! (This is online mode!)
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
                Logger.error("An error occurred whilst initializing the world!", throwable);
            } else {
                defaultInstance.setChunkLoader(new PolarLoader(polarWorld));
                defaultInstance.setChunkSupplier(LightingChunk::new);
                defaultInstance.enableAutoChunkLoad(true);
                Logger.info("World loaded!");
            }
        });
    }

    /**
     * Completes nonessential startup tasks for the server
     *
     * @param start The time the server started
     */
    public static void completeNonEssentialTasks(long start) {
        BlockPlacementUtils.init();

        natsManager = new NatsManager();
        if (!flags.contains("--ci-test")) natsManager.setup(); // don't connect to NATS in compile and run checks

        // commands
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSignedCommandChatPacket.class, (packet, p) -> MinecraftServer.getPacketListenerManager().processClientPacket(new ClientCommandChatPacket(packet.message()), p.getPlayerConnection(), p.getPlayerConnection().getConnectionState()));

        Logger.info("Initializing database");
        databaseManager = new DatabaseManager();
        databaseManager.setupDatabases().whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst initializing the database!", throwable);
                return;
            }

            Thread.ofVirtual().name("WorldLoader").start(Cytosis::loadWorld);

            Logger.info("Database initialized!");
            Logger.info("Setting up event handlers");
            eventHandler = new EventHandler(MinecraftServer.getGlobalEventHandler());
            eventHandler.init();

            Logger.info("Loading player preferences");
            preferenceManager = new PreferenceManager();

            Logger.info("Initializing server events");
            ServerEventListeners.initServerEvents();

            Logger.info("Loading vanish manager!");
            vanishManager = new VanishManager();
            Runtime.getRuntime().addShutdownHook(new Thread(Cytosis::shutdownHandler));
            MinecraftServer.getSchedulerManager().buildShutdownTask(Cytosis::shutdownHandler);

            Logger.info("Initializing Plugin Manager!");
            pluginManager = new PluginManager();
            Logger.info("Loading plugins!");

            Thread.ofVirtual().name("CytosisPluginLoader").start(() -> {
                try {
                    pluginManager.loadPlugins(Path.of("plugins"));
                } catch (Exception e) {
                    Logger.error("An error occurred whilst loading plugins!", e);
                }
            });

            Logger.info("Starting Player list manager");
            playerListManager = new PlayerListManager();

            messagingManager = new MessagingManager();
            messagingManager.initialize().whenComplete((unused, th) -> {
                if (th != null) {
                    Logger.error("An error occurred whilst initializing the messaging manager!", th);
                } else {
                    Logger.info("Messaging manager initialized!");
                }
            });

            Logger.info("Starting Friend manager!");
            friendManager = new FriendManager();
            friendManager.init();

            Logger.info("Initializing Rank Manager");
            rankManager = new RankManager();
            rankManager.init();

            Logger.info("Creating sideboard manager!");
            sideboardManager = new SideboardManager();
            sideboardManager.updateBoards();

            Logger.info("Starting NPC manager!");
            npcManager = new NPCManager();

            try {
                if (CytosisSettings.SERVER_PROXY_MODE) {
                    Logger.info("Loading network setup!");
                    cytonicNetwork = new CytonicNetwork();
                    cytonicNetwork.importData();
                    cytonicNetwork.getServers().put(SERVER_ID, new CytonicServer(Utils.getServerIP(), SERVER_ID, CytosisSettings.SERVER_PORT));
                }
            } catch (Exception e) {
                Logger.error("An error occurred whilst loading network setup!", e);
            }

            Logger.info("Starting cooldown managers");
            networkCooldownManager = new NetworkCooldownManager(databaseManager.getRedisDatabase());
            networkCooldownManager.importFromRedis();
            Logger.info("Started network cooldown manager");

            Logger.info("Initializing server commands");
            commandHandler = new CommandHandler();
            commandHandler.setupConsole();
            commandHandler.registerCytosisCommands();

            Logger.info("starting actionbar manager");
            actionbarManager = new ActionbarManager();
            actionbarManager.init();

            Logger.info("Starting Snooper Manager");
            snooperManager = new SnooperManager();
            Logger.info("Loading snooper channels from redis");
            snooperManager.loadChannelsFromRedis();
            Logger.info("Loading Cytosis snoops");
            // load snoops
            snooperManager.registerChannel(CytosisSnoops.PLAYER_BANS);

            // Gui Framework...
            ClickableItemRegistry.getInstance().registerAll();


            // Start the server
            Logger.info("Server started on port " + CytosisSettings.SERVER_PORT + "");
            minecraftServer.start("0.0.0.0", CytosisSettings.SERVER_PORT);
            MinecraftServer.getExceptionManager().setExceptionHandler(e -> Logger.error("Uncaught exception", e));
            try {
                natsManager.sendStartup();
            } catch (Exception e) {
                Logger.error("ERROR: ", e);
            }
            long end = System.currentTimeMillis();
            Logger.info("Server started in " + (end - start) + "ms!");
            Logger.info("Server id = " + SERVER_ID + "");


            if (flags.contains("--ci-test")) {
                Logger.info("Stopping server due to '--ci-test' flag.");
                MinecraftServer.stopCleanly();
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
        messagingManager.shutdown();
        databaseManager.shutdown();
        sideboardManager.shutdown();
        pluginManager.unloadPlugins();
        getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.kick(Msg.mm("<red>The server is shutting down.")));
    }

    public static CytonicServer currentServer() {
        return new CytonicServer(Utils.getServerIP(), SERVER_ID, CytosisSettings.SERVER_PORT);
    }
}