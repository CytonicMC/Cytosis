package net.cytonic.cytosis;

import net.cytonic.cytosis.data.Database;
import net.cytonic.cytosis.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Manager {
    private final ExecutorService worker;
    private Database database;

    public Manager() {
        this.worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisManagerWorker")
                .uncaughtExceptionHandler((t, e) -> Logger.error(STR."An uncaught exception occoured on the thread: \{t.getName()}", e)).factory());
    }

    public void shutdown() {
        worker.submit(() -> {
            database.disconnect();
            Logger.info("Good night!");
        });
    }

    public void setupDatabase() {
        worker.submit(() -> {
            database = new Database();
            database.connect();
            database.createChatTable();
        });
    }

    public void setupBedwars() {}


    public Database getDatabase() {return database;}
}
