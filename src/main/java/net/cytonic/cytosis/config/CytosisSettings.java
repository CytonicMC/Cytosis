package net.cytonic.cytosis.config;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.PosSerializer;
import net.minestom.server.coordinate.Pos;

import java.util.Map;

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
     * Loads the config from a config map
     *
     * @param config The config map of key value pairs
     */
    public static void inportConfig(Map<String, Object> config) {
        Logger.info("Importing config!");
        config.forEach((key, value) -> {
            try {
                switch (key.replace("\"", "")) {
                    // database
                    case "database.user" -> DATABASE_USER = (String) value;
                    case "database.password" -> DATABASE_PASSWORD = (String) value;
                    case "database.host" -> DATABASE_HOST = (String) value;
                    case "database.port" -> DATABASE_PORT = toInt(value);
                    case "database.name" -> DATABASE_NAME = (String) value;
                    //server
                    case "server.secret" -> SERVER_SECRET = (String) value;
                    case "server.port" -> SERVER_PORT = toInt(value);
                    case "server.world_name" -> SERVER_WORLD_NAME = (String) value;
                    case "server.spawn_point" -> SERVER_SPAWN_POS = PosSerializer.deserialize((String) value);

                    // Redis
                    case "redis.port" -> REDIS_PORT = toInt(value);
                    case "redis.host" -> REDIS_HOST = (String) value;
                    case "redis.password" -> REDIS_PASSWORD = (String) value;

                    case "nats.host" -> NATS_HOSTNAME = (String) value;
                    case "nats.password" -> NATS_PASSWORD = (String) value;
                    case "nats.username" -> NATS_USERNAME = (String) value;
                    case "nats.port" -> NATS_PORT = toInt(value);

                    default -> { /*Do nothing*/ }
                }
            } catch (ClassCastException e) {
                Logger.error("Could not import config key: " + key, e);
            }
        });
        Logger.info("Config imported!");
    }

    private static int toInt(Object key) {
        return Integer.parseInt(key.toString());
    }

    /**
     * Load settings from environment variables
     */
    public static void loadEnvironmentVariables() {
        Logger.info("Loading environment variables!");
        // database

        if (System.getenv("DATABASE_USER") != null) DATABASE_USER = System.getenv("DATABASE_USER");
        if (System.getenv("DATABASE_PASSWORD") != null) DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD");
        if (System.getenv("DATABASE_HOST") != null) DATABASE_HOST = System.getenv("DATABASE_HOST");
        if (System.getenv("DATABASE_PORT") != null) DATABASE_PORT = Integer.parseInt(System.getenv("DATABASE_PORT"));
        if (System.getenv("DATABASE_NAME") != null) DATABASE_NAME = System.getenv("DATABASE_NAME");

        //server

        if (System.getenv("SERVER_SECRET") != null) CytosisSettings.SERVER_SECRET = System.getenv("SERVER_SECRET");
        if (System.getenv("SERVER_PORT") != null)
            CytosisSettings.SERVER_PORT = Integer.parseInt(System.getenv("SERVER_PORT"));
        if (System.getenv("SERVER_WORLD_NAME") != null)
            CytosisSettings.SERVER_WORLD_NAME = System.getenv("SERVER_WORLD_NAME");
        if (System.getenv("SERVER_SPAWN_POINT") != null)
            CytosisSettings.SERVER_SPAWN_POS = PosSerializer.deserialize(System.getenv("SERVER_SPAWN_POINT"));

        // redis
        if (!(System.getenv("REDIS_HOST") == null)) REDIS_HOST = System.getenv("REDIS_HOST");
        if (!(System.getenv("REDIS_PORT") == null)) REDIS_PORT = Integer.parseInt(System.getenv("REDIS_PORT"));
        if (!(System.getenv("REDIS_PASSWORD") == null)) REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");

        // Nats
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
        if (System.getProperty("DATABASE_PASSWORD") != null)
            DATABASE_PASSWORD = System.getProperty("DATABASE_PASSWORD");
        if (System.getProperty("DATABASE_HOST") != null) DATABASE_HOST = System.getProperty("DATABASE_HOST");
        if (System.getProperty("DATABASE_PORT") != null)
            DATABASE_PORT = Integer.parseInt(System.getProperty("DATABASE_PORT"));
        if (System.getProperty("DATABASE_NAME") != null) DATABASE_NAME = System.getProperty("DATABASE_NAME");
        //server

        if (System.getProperty("SERVER_SECRET") != null)
            CytosisSettings.SERVER_SECRET = System.getProperty("SERVER_SECRET");
        if (System.getProperty("SERVER_PORT") != null)
            CytosisSettings.SERVER_PORT = Integer.parseInt(System.getProperty("SERVER_PORT"));
        if (System.getProperty("SERVER_WORLD_NAME") != null)
            CytosisSettings.SERVER_WORLD_NAME = System.getProperty("SERVER_WORLD_NAME");
        if (System.getProperty("SERVER_SPAWN_POINT") != null)
            CytosisSettings.SERVER_SPAWN_POS = PosSerializer.deserialize(System.getProperty("SERVER_SPAWN_POINT"));
        // redis
        if (System.getProperty("REDIS_HOST") != null) REDIS_HOST = System.getProperty("REDIS_HOST");
        if (System.getProperty("REDIS_PORT") != null) REDIS_PORT = Integer.parseInt(System.getProperty("REDIS_PORT"));
        if (System.getProperty("REDIS_PASSWORD") != null) REDIS_PASSWORD = System.getProperty("REDIS_PASSWORD");

        // Nats
        if (System.getProperty("NATS_HOSTNAME") != null) NATS_HOSTNAME = System.getProperty("NATS_HOSTNAME");
        if (System.getProperty("NATS_USERNAME") != null) NATS_USERNAME = System.getProperty("NATS_USERNAME");
        if (System.getProperty("NATS_PASSWORD") != null) NATS_PASSWORD = System.getProperty("NATS_PASSWORD");
        if (System.getProperty("NATS_PORT") != null) NATS_PORT = Integer.parseInt(System.getProperty("NATS_PORT"));
    }
}