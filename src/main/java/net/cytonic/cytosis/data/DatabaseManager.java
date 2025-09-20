package net.cytonic.cytosis.data;

import lombok.Getter;
import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.logging.Logger;

/**
 * A class managing databases
 */
@Getter
public class DatabaseManager implements Bootstrappable {

    private MysqlDatabase mysqlDatabase;
    private RedisDatabase redisDatabase;

    /**
     * The default constructor
     */
    public DatabaseManager() {
    }

    @Override
    public void init() {
        setupDatabases();
    }

    /**
     * Disconnects from the databases
     */
    @Override
    public void shutdown() {
        mysqlDatabase.disconnect();
        redisDatabase.disconnect();
        Logger.info("Good night!");
    }

    /**
     * Sets up the databases by creating tables and creating connections
     */
    public void setupDatabases() {
        Logger.info("Connecting to MySQL Database.");
        mysqlDatabase = new MysqlDatabase();

        mysqlDatabase.connect();
        mysqlDatabase.createTables();

        Logger.info("Connecting to the Redis Database.");
        redisDatabase = new RedisDatabase(); // it handles initialization in the constructor


        Logger.info("All Databases connected.");
    }
}