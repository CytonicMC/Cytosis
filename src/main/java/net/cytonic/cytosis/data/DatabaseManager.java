package net.cytonic.cytosis.data;

import lombok.Getter;
import net.cytonic.cytosis.logging.Logger;

@Getter
public class DatabaseManager {

    private MysqlDatabase mysqlDatabase;
    private RedisDatabase redisDatabase;

    public DatabaseManager() {
    }

    public void shutdown() {
        mysqlDatabase.disconnect();
        redisDatabase.disconnect();
        Logger.info("Good night!");
    }

    public void setupDatabases() {
        Logger.info("Connecting to MySQL Database.");
        mysqlDatabase = new MysqlDatabase();
        mysqlDatabase.connect();
        mysqlDatabase.createTables();

        Logger.info("Connecting to the Redis Database.");
        try {
            redisDatabase = new RedisDatabase(); // it handles itnitialization in the constructor
        } catch (Exception ex) {
            Logger.error("An error occured!", ex);
        }
        Logger.info("All databases connected.");
    }

}