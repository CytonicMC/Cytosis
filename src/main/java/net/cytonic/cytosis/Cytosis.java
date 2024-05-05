package net.cytonic.cytosis;

import net.cytonic.cytosis.commands.CommandHandler;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.events.ServerEventListeners;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.MessagingManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.permission.Permission;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Cytosis {

    // manager stuff
    private static MinecraftServer MINECRAFT_SERVER;
    private static InstanceManager INSTANCE_MANAGER;
    private static InstanceContainer DEFAULT_INSTANCE;
    private static EventHandler EVENT_HANDLER;
    private static ConnectionManager CONNECTION_MANAGER;
    private static CommandManager COMMAND_MANAGER;
    private static CommandHandler COMMAND_HANDLER;
    private static FileManager FILE_MANAGER;
    private static MessagingManager MESSAGE_MANAGER;

    private static ConsoleSender CONSOLE_SENDER;

    public static void main(String[] args) {
        //todo: Add flags for special server functionality (ie env variables)
        long start = System.currentTimeMillis();
        // Initialize the server
        Logger.info("Starting server.");
        MINECRAFT_SERVER = MinecraftServer.init();
        MinecraftServer.setBrandName("Cytosis");

        Logger.info("Initializing Mojang Authentication");
        MojangAuth.init(); //VERY IMPORTANT! (This is online mode!)

        Logger.info("Starting instance manager.");
        INSTANCE_MANAGER = MinecraftServer.getInstanceManager();

        Logger.info("Starting connection manager.");
        CONNECTION_MANAGER = MinecraftServer.getConnectionManager();


        // Commands
        Logger.info("Starting command manager.");
        COMMAND_MANAGER = MinecraftServer.getCommandManager();

        Logger.info("Setting console command sender.");
        CONSOLE_SENDER = COMMAND_MANAGER.getConsoleSender();
        CONSOLE_SENDER.addPermission(new Permission("*"));

        // instances
        Logger.info("Creating instance container");
        DEFAULT_INSTANCE = INSTANCE_MANAGER.createInstanceContainer();

        Logger.info("Creating file manager");
        FILE_MANAGER = new FileManager();

        // Everything after this point depends on config contents
        Logger.info("Initializing file manager");
        FILE_MANAGER.init().whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst initializing the file manager!", throwable);
            } else {
                Logger.info("File manager initialized!");
                Logger.info("Completing nonessential startup tasks.");
                completeNonEssentialTasks(start);
            }
        });
    }

    public static EventHandler getEventHandler() {
        return EVENT_HANDLER;
    }

    public static InstanceContainer getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static ConnectionManager getConnectionManager() {
        return CONNECTION_MANAGER;
    }

    public static CommandManager getCommandManager() {
        return COMMAND_MANAGER;
    }

    public static Set<Player> getOnlinePlayers() {
        Set<Player> players = new HashSet<>();
        INSTANCE_MANAGER.getInstances().forEach(instance -> players.addAll(instance.getPlayers()));
        return players;
    }

    public static Optional<Player> getPlayer(String username) {
        Player target = null;
        for (Player onlinePlayer : getOnlinePlayers()) {
            if (onlinePlayer.getUsername().equals(username)) target = onlinePlayer;
        }
        return Optional.ofNullable(target);
    }

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

    public static ConsoleSender getConsoleSender() {
        return CONSOLE_SENDER;
    }

    public static void completeNonEssentialTasks(long start) {
        // basic world generator
        Logger.info("Generating basic world");
        DEFAULT_INSTANCE.setGenerator(unit -> unit.modifier().fillHeight(0, 1, Block.WHITE_STAINED_GLASS));
        DEFAULT_INSTANCE.setChunkSupplier(LightingChunk::new);

        Logger.info("Setting up event handlers");
        EVENT_HANDLER = new EventHandler(MinecraftServer.getGlobalEventHandler());

        Logger.info("Initializing server events");
        ServerEventListeners.initServerEvents();

        Logger.info("Initializing server commands");
        COMMAND_HANDLER = new CommandHandler();
        COMMAND_HANDLER.setupConsole();
        COMMAND_HANDLER.registerCystosisCommands();

        MESSAGE_MANAGER = new MessagingManager();
        MESSAGE_MANAGER.initialize().whenComplete((_, throwable) -> {
            if (throwable != null) {
                Logger.error("An error occurred whilst initializing the messaging manager!", throwable);
            } else {
                Logger.info("Messaging manager initialized!");
            }
        });

        // Start the server
        Logger.info("Server started on port 25565");
        MINECRAFT_SERVER.start("0.0.0.0", 25565);
        long end = System.currentTimeMillis();
        Logger.info(StringTemplate.STR."Server started in \{end - start}ms!");
    }
}