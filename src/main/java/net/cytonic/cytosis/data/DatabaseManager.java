package net.cytonic.cytosis.data;

import lombok.Getter;
import net.cytonic.cytosis.logging.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * A class managing databases
 */
@Getter
public class DatabaseManager {

    private MysqlDatabase mysqlDatabase;
    private RedisDatabase redisDatabase;

    /**
     * The default constructor
     */
    public DatabaseManager() {
    }

    /**
     * Disconnects from the databases
     */
    public void shutdown() {
        mysqlDatabase.disconnect();
        redisDatabase.disconnect();
        Logger.info("Good night!");
    }

    /**
     * Sets up the databases by creating tables and creating connections
     *
     * @return A future that completes once all databases are connected
     */
    public CompletableFuture<Void> setupDatabases() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Logger.info("Connecting to MySQL Database.");
        mysqlDatabase = new MysqlDatabase();
        mysqlDatabase.connect().whenComplete((_, _) -> future.complete(null));
        mysqlDatabase.createTables();

        Logger.info("Connecting to the Redis Database.");
        try {
            redisDatabase = new RedisDatabase(); // it handles initialization in the constructor
        } catch (Exception ex) {
            Logger.error("An error occurred!", ex);
        }

        Logger.info("All Databases connected.");
        return future;
    }
}