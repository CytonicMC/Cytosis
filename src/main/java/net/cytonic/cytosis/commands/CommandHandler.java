package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.chatting.*;
import net.cytonic.cytosis.commands.debug.CooldownCommand;
import net.cytonic.cytosis.commands.debug.PreferenceCommand;
import net.cytonic.cytosis.commands.defaultMinecraft.GamemodeCommand;
import net.cytonic.cytosis.commands.defaultMinecraft.TeleportCommand;
import net.cytonic.cytosis.commands.moderation.*;
import net.cytonic.cytosis.commands.movement.LobbyCommand;
import net.cytonic.cytosis.commands.movement.PlayCommand;
import net.cytonic.cytosis.commands.server.*;
import net.cytonic.cytosis.commands.staff.*;
import net.cytonic.cytosis.commands.staff.snooper.SnooperCommand;
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
        cm.register(
                new GamemodeCommand(),
                new RankCommand(),
                new BanCommand(),
                new ChatChannelCommand(),
                new StopCommand(),
                new ServerCommand(),
                new CreateInstanceCommand(),
                new ShutdownInstancesCommand(),
                new PodDetailsCommand(),
                new TeleportCommand(),
                new FindCommand(),
                new PreferenceCommand(),
                new ServerAlertsCommand(),
                new FlyCommand(),
                new BroadcastCommand(),
                new HelpCommand(),
                new AllChatCommand(),
                new TimeCommand(),
                new VersionCommand(),
                new PingCommand(),
                new FriendCommand(),
                new ClearchatCommand(),
                new VanishCommand(),
                new IgnoreChatChannelCommand(),
                new UnbanCommand(),
                new MuteCommand(),
                new UnmuteCommand(),
                new KickCommand(),
                new MsgCommand(),
                new WarnCommand(),
                new CooldownCommand(Cytosis.getNetworkCooldownManager()),
                new LoopCommand(),
                new RecalculatePermissions(),
                new YoinkCommand(),
                new ReplyCommand(),
                new SnooperCommand(),
                new PlayCommand(),
                new LobbyCommand(),
                new WhereAmiCommand()
        );
    }

    /**
     * Sends a packet to the player to recalculate command permissions
     *
     * @param player The player to send the packet to
     */
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
