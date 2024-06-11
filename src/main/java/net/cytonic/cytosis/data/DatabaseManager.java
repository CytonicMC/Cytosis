package net.cytonic.cytosis.data;

import lombok.Getter;
import net.cytonic.cytosis.logging.Logger;

import java.util.concurrent.CompletableFuture;

@Getter
public class DatabaseManager {

    private MysqlDatabase mysqlDatabase;
    private RedisDatabase redisDatabase;

    public DatabaseManager() {}

    public void shutdown() {
        mysqlDatabase.disconnect();
        Logger.info("Good night!");
    }

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
        Logger.info("All mysqlDatabases connected.");
        return future;
    }
}