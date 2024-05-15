package net.cytonic.cytosis;

import lombok.Getter;
import net.cytonic.cytosis.commands.CommandHandler;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.DatabaseManager;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.events.ServerEventListeners;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.MessagingManager;
import net.cytonic.cytosis.ranks.RankManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.permission.Permission;

import java.util.*;


@Getter
public class Cytosis {

    // manager stuff
    @Getter
    private static MinecraftServer minecraftServer;
    @Getter
    private static InstanceManager instanceManager;
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

    private static List<String> FLAGS;

    public static void main(String[] args) {
        FLAGS = List.of(args);
        //todo: Add flags for special server functionality (ie env variables)
        long start = System.currentTimeMillis();
        // Initialize the server
        Logger.info("Starting server.");
        minecraftServer = MinecraftServer.init();
        MinecraftServer.setBrandName("Cytosis");

        Logger.info("Starting instance manager.");
        instanceManager = MinecraftServer.getInstanceManager();

        Logger.info("Starting connection manager.");
        connectionManager = MinecraftServer.getConnectionManager();


        Logger.info("Starting manager.");
        databaseManager = new DatabaseManager();

        // Commands
        Logger.info("Starting command manager.");
        commandManager = MinecraftServer.getCommandManager();

        Logger.info("Setting console command sender.");
        consoleSender = commandManager.getConsoleSender();
        consoleSender.addPermission(new Permission("*"));

        // instances
        Logger.info("Creating instance container");
        defaultInstance = instanceManager.createInstanceContainer();

        Logger.info("Creating file manager");
        fileManager = new FileManager();

        // Everything after this point depends on config contents
        Logger.info("Initializing file manager");
        fileManager.init().whenComplete((_, throwable) -> {
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
                completeNonEssentialTasks(start);
            }
        });
    }

    public static Set<Player> getOnlinePlayers() {
        Set<Player> players = new HashSet<>();
        instanceManager.getInstances().forEach(instance -> players.addAll(instance.getPlayers()));
        return players;
    }

    /**
     * Gets the player if they are on THIS instance, by username
     *
     * @param username The username to fetch the player by
     * @return The optional holding the player, if it exists.
     */
    public static Optional<Player> getPlayer(String username) {
        Player target = null;
        for (Player onlinePlayer : getOnlinePlayers())
            if (onlinePlayer.getUsername().equals(username)) target = onlinePlayer;
        return Optional.ofNullable(target);
    }

    /**
     * Gets the player if they are on THIS instance, by UUID
     * @param uuid The uuid to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static Optional<Player> getPlayer(UUID uuid) {
        Player target = null;
        for (Player onlinePlayer : getOnlinePlayers()) {
            if (onlinePlayer.getUuid() == uuid) target = onlinePlayer;
        }
        return Optional.ofNullable(target);
    }

    public static void opPlayer(Player player) {
        player.addPermission(new Permission("*")); // give them every permission
    }

    public static void deopPlayer(Player player) {
        player.removePermission("*"); // remove every permission
    }

    public static void mojangAuth() {
        Logger.info("Initializing Mojang Authentication");
        MojangAuth.init(); //VERY IMPORTANT! (This is online mode!)
    }

    public static void completeNonEssentialTasks(long start) {
        // basic world generator
        Logger.info("Generating basic world");
        defaultInstance.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.WHITE_STAINED_GLASS));
        defaultInstance.setChunkSupplier(LightingChunk::new);

        Logger.info("Setting up event handlers");
        eventHandler = new EventHandler(MinecraftServer.getGlobalEventHandler());
        eventHandler.init();

        Logger.info("Initializing server events");
        ServerEventListeners.initServerEvents();

        Logger.info("Initializing database");
        databaseManager.setupDatabase();

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> databaseManager.shutdown());

        Logger.info("Initializing server commands");
        commandHandler = new CommandHandler();
        commandHandler.setupConsole();
        commandHandler.registerCystosisCommands();

        messagingManager = new MessagingManager();
        messagingManager.initialize().whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst initializing the messaging manager!", throwable);
            } else {
                Logger.info("Messaging manager initialized!");
            }
        });

        Logger.info("Initializing Rank Manager");
        rankManager = new RankManager();
        rankManager.init();

        // Start the server
        Logger.info(STR."Server started on port \{CytosisSettings.SERVER_PORT}");
        minecraftServer.start("0.0.0.0", CytosisSettings.SERVER_PORT);
      
        long end = System.currentTimeMillis();
        Logger.info(STR."Server started in \{end - start}ms!");

        if (FLAGS.contains("--ci-test")) {
            Logger.info("Stopping server due to '--ci-test' flag.");
            MinecraftServer.stopCleanly();
        }
    }
}