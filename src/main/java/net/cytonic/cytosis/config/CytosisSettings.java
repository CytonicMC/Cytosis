package net.cytonic.cytosis.config;

import net.minestom.server.coordinate.Pos;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.PosSerializer;

/**
 * This class is used to store all the cached configuration values.
 */
public final class CytosisSettings {
    // Database
    /**
     * Database username
     */
    public static String DATABASE_USER = "";
    /**
     * Database password
     */
    public static String DATABASE_PASSWORD = "";
    /**
     * Hostname of the database server
     */
    public static String DATABASE_HOST = "";
    /**
     * Database port
     */
    public static int DATABASE_PORT = 3306;
    /**
     * Name of the database to use
     */
    public static String DATABASE_NAME = "";

    // server
    /**
     * The velocity forwarding secret
     */
    public static String SERVER_SECRET = "cannot-be-empty";
    /**
     * The port to start on
     */
    public static int SERVER_PORT = 25565;
    /**
     * The name of the world to load from the database
     */
    public static String SERVER_WORLD_NAME = "";
    /**
     * The pos for players to spawn at
     */
    public static Pos SERVER_SPAWN_POS = new Pos(0, 1, 0);
    /**
     * The redis port
     */
    public static int REDIS_PORT = 6379;

    //Redis
    /**
     * The redis hostname
     */
    public static String REDIS_HOST = "";
    /**
     * The redis password
     */
    public static String REDIS_PASSWORD = "";
    /*
     * The configuration for the NATS message tool
     */
    public static String NATS_HOSTNAME = "";
    public static int NATS_PORT = 4222;
    public static String NATS_PASSWORD = "";
    public static String NATS_USERNAME = "";

    private CytosisSettings() {
    }

    /**
     * Loads the config from a {@link ConfigurationNode}
     *
     * @param node The configuration node
     */
    public static void importConfig(ConfigurationNode node) {
        Logger.info("Importing config!");
        try {
            //database
            DATABASE_USER = node.node("database", "user").getString();
            DATABASE_PASSWORD = node.node("database", "password").getString();
            DATABASE_HOST = node.node("database", "host").getString();
            DATABASE_PORT = node.node("database", "port").getInt();
            DATABASE_NAME = node.node("database", "name").getString();
            //server
            SERVER_SECRET = node.node("server", "secret").getString();
            SERVER_PORT = node.node("server", "port").getInt();
            SERVER_WORLD_NAME = node.node("server", "world_name").getString();
            SERVER_SPAWN_POS = node.node("server", "spawn_point").get(Pos.class);
            //redis
            REDIS_PORT = node.node("redis", "port").getInt();
            REDIS_HOST = node.node("redis", "host").getString();
            REDIS_PASSWORD = node.node("redis", "password").getString();
            //nats
            NATS_HOSTNAME = node.node("nats", "host").getString();
            NATS_PASSWORD = node.node("nats", "password").getString();
            NATS_USERNAME = node.node("nats", "username").getString();
            NATS_PORT = node.node("nats", "port").getInt();
        } catch (SerializationException e) {
            Logger.error("Could not import config!", e);
        }
        Logger.info("Config imported!");
    }

    /**
     * Load settings from environment variables
     */
    public static void loadEnvironmentVariables() {
        Logger.info("Loading environment variables!");
        if (System.getenv("DATABASE_USER") != null) DATABASE_USER = System.getenv("DATABASE_USER");
        if (System.getenv("DATABASE_PASSWORD") != null) DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD");
        if (System.getenv("DATABASE_HOST") != null) DATABASE_HOST = System.getenv("DATABASE_HOST");
        if (System.getenv("DATABASE_PORT") != null) DATABASE_PORT = Integer.parseInt(System.getenv("DATABASE_PORT"));
        if (System.getenv("DATABASE_NAME") != null) DATABASE_NAME = System.getenv("DATABASE_NAME");
        //server
        if (System.getenv("SERVER_SECRET") != null) SERVER_SECRET = System.getenv("SERVER_SECRET");
        if (System.getenv("SERVER_PORT") != null) SERVER_PORT = Integer.parseInt(System.getenv("SERVER_PORT"));
        if (System.getenv("SERVER_WORLD_NAME") != null) SERVER_WORLD_NAME = System.getenv("SERVER_WORLD_NAME");
        if (System.getenv("SERVER_SPAWN_POINT") != null) {
            SERVER_SPAWN_POS = PosSerializer.deserialize(System.getenv("SERVER_SPAWN_POINT"));
        }
        // redis
        if (!(System.getenv("REDIS_HOST") == null)) REDIS_HOST = System.getenv("REDIS_HOST");
        if (!(System.getenv("REDIS_PORT") == null)) REDIS_PORT = Integer.parseInt(System.getenv("REDIS_PORT"));
        if (!(System.getenv("REDIS_PASSWORD") == null)) REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");
        // nats
        if (System.getenv("NATS_HOSTNAME") != null) NATS_HOSTNAME = System.getenv("NATS_HOSTNAME");
        if (System.getenv("NATS_USERNAME") != null) NATS_USERNAME = System.getenv("NATS_USERNAME");
        if (System.getenv("NATS_PASSWORD") != null) NATS_PASSWORD = System.getenv("NATS_PASSWORD");
        if (System.getenv("NATS_PORT") != null) NATS_PORT = Integer.parseInt(System.getenv("NATS_PORT"));
    }

    /**
     * Load settings from command args (System Properties)
     */
    public static void loadCommandArgs() {
        Logger.info("Loading command args!");
        // database
        if (System.getProperty("DATABASE_USER") != null) DATABASE_USER = System.getProperty("DATABASE_USER");
        if (System.getProperty("DATABASE_PASSWORD") != null) {
            DATABASE_PASSWORD = System.getProperty("DATABASE_PASSWORD");
        }
        if (System.getProperty("DATABASE_HOST") != null) DATABASE_HOST = System.getProperty("DATABASE_HOST");
        if (System.getProperty("DATABASE_PORT") != null) {
            DATABASE_PORT = Integer.parseInt(System.getProperty("DATABASE_PORT"));
        }
        if (System.getProperty("DATABASE_NAME") != null) DATABASE_NAME = System.getProperty("DATABASE_NAME");
        //server
        if (System.getProperty("SERVER_SECRET") != null) SERVER_SECRET = System.getProperty("SERVER_SECRET");
        if (System.getProperty("SERVER_PORT") != null) {
            SERVER_PORT = Integer.parseInt(System.getProperty("SERVER_PORT"));
        }
        if (System.getProperty("SERVER_WORLD_NAME") != null) {
            SERVER_WORLD_NAME = System.getProperty("SERVER_WORLD_NAME");
        }
        if (System.getProperty("SERVER_SPAWN_POINT") != null) {
            SERVER_SPAWN_POS = PosSerializer.deserialize(System.getProperty("SERVER_SPAWN_POINT"));
        }
        // redis
        if (System.getProperty("REDIS_HOST") != null) REDIS_HOST = System.getProperty("REDIS_HOST");
        if (System.getProperty("REDIS_PORT") != null) REDIS_PORT = Integer.parseInt(System.getProperty("REDIS_PORT"));
        if (System.getProperty("REDIS_PASSWORD") != null) REDIS_PASSWORD = System.getProperty("REDIS_PASSWORD");
        // nats
        if (System.getProperty("NATS_HOSTNAME") != null) NATS_HOSTNAME = System.getProperty("NATS_HOSTNAME");
        if (System.getProperty("NATS_USERNAME") != null) NATS_USERNAME = System.getProperty("NATS_USERNAME");
        if (System.getProperty("NATS_PASSWORD") != null) NATS_PASSWORD = System.getProperty("NATS_PASSWORD");
        if (System.getProperty("NATS_PORT") != null) NATS_PORT = Integer.parseInt(System.getProperty("NATS_PORT"));
    }
}