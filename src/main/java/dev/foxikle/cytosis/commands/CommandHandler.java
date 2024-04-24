package dev.foxikle.cytosis.commands;

import dev.foxikle.cytosis.Cytosis;
import net.minestom.server.command.CommandManager;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandHandler {

    private final ScheduledExecutorService worker;
    private final Scanner CONSOLE_IN = new Scanner(System.in);
    private final Object consoleLock = new Object();

    public CommandHandler() {
        this.worker = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("CytosisConsoleWorker").factory());
    }

    public void registerCystosisCommands() {
        CommandManager cm = Cytosis.getCommandManager();
        cm.register(new GamemodeCommand());
        cm.register(new OperatorCommand());
    }

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
