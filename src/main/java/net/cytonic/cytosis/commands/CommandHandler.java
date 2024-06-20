package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.moderation.BanCommand;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A class that handles the commands, their execution, and allegedly a console.
 */
public class CommandHandler {

    private final ScheduledExecutorService worker;
    private final Scanner CONSOLE_IN = new Scanner(System.in);
    private final Object consoleLock = new Object();

    /**
     * Creates a command handler and sets up the worker
     */
    public CommandHandler() {
        this.worker = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("CytosisConsoleWorker").factory());
    }

    /**
     * Registers the default Cytosis commands
     */
    public void registerCytosisCommands() {
        CommandManager cm = Cytosis.getCommandManager();
        cm.register(new GamemodeCommand());
        cm.register(new RankCommand());
        cm.register(new BanCommand());
        cm.register(new ChatChannelCommand());
        cm.register(new StopCommand());
        cm.register(new ServerCommand());
        cm.register(new CreateInstanceCommand());
        cm.register(new ShutdownInstancesCommand());
        cm.register(new PodDetailsCommand());
    }

    /**
     * Sends a packet to the player to recalculate command permissions
     *
     * @param player The player to send the packet to
     */
    @SuppressWarnings("UnstableApiUsage")
    public void recalculateCommands(Player player) {
        player.sendPacket(Cytosis.getCommandManager().createDeclareCommandsPacket(player));
    }

    /**
     * Sets up the console so commands can be executed from there
     */
    public void setupConsole() {
        worker.scheduleAtFixedRate(() -> {
            if (CONSOLE_IN.hasNext()) {
                String command = CONSOLE_IN.nextLine();

                synchronized (consoleLock) {
                    Cytosis.getCommandManager().getDispatcher().execute(Cytosis.getConsoleSender(), command);
                }
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }
}