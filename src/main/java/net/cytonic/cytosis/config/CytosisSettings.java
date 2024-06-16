package net.cytonic.cytosis.config;

import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.PosSerializer;
import net.minestom.server.coordinate.Pos;

import java.util.Map;

/**
 * This class is used to store all the cached configuration values.
 */
public final class CytosisSettings {
    /**
     * Defualt constructor
     */
    private CytosisSettings() {
    }

    // Logging
    /**
     * Should the server log player joins
     */
    public static boolean LOG_PLAYER_JOINS = true;
    /**
     * Should the server log player quits
     */
    public static boolean LOG_PLAYER_QUITS = true;
    /**
     * Should the server log player commands
     */
    public static boolean LOG_PLAYER_COMMANDS = true;
    /**
     * Should the server log player chat messages
     */
    public static boolean LOG_PLAYER_CHAT = true;

    // Database
    /**
     * Should the server use the database
     */
    public static boolean DATABASE_ENABLED = true;
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
    /**
     * Use SSL?
     */
    public static boolean DATABASE_USE_SSL = false;

    // server
    /**
     * Should Cytosis run in proxy mode?
     */
    public static boolean SERVER_PROXY_MODE = false;
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
     * Should Cytosis use RabbitMQ?
     */
    public static boolean RABBITMQ_ENABLED = false;
    /**
     * hostname of the RabbitMQ server
     */
    public static String RABBITMQ_HOST = "";
    /**
     * RabbitMQ Password
     */
    public static String RABBITMQ_PASSWORD = "";
    /**
     * RabbitMQ server username
     */
    public static String RABBITMQ_USERNAME = "";
    /**
     * The port to connect to RabbitMQ on
     */
    public static int RABBITMQ_PORT = 5672;

    //Redis
    /**
     * The redis port
     */
    public static int REDIS_PORT = 6379;
    /**
     * The redis hostname
     */
    public static String REDIS_HOST = "";
    /**
     * The redis password
     */
    public static String REDIS_PASSWORD = "";

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
                    // logging
                    case "logging.player_join" -> LOG_PLAYER_JOINS = (boolean) value;
                    case "logging.player_quit" -> LOG_PLAYER_QUITS = (boolean) value;
                    case "logging.player_command" -> LOG_PLAYER_COMMANDS = (boolean) value;
                    case "logging.player_chat" -> LOG_PLAYER_CHAT = (boolean) value;
                    // database
                    case "database.enabled" -> DATABASE_ENABLED = (boolean) value;
                    case "database.user" -> DATABASE_USER = (String) value;
                    case "database.password" -> DATABASE_PASSWORD = (String) value;
                    case "database.host" -> DATABASE_HOST = (String) value;
                    case "database.port" -> DATABASE_PORT = toInt(value);
                    case "database.name" -> DATABASE_NAME = (String) value;
                    case "database.use_ssl" -> DATABASE_USE_SSL = (boolean) value;
                    // server
                    case "server.proxy_mode" -> SERVER_PROXY_MODE = (boolean) value;
                    case "server.secret" -> SERVER_SECRET = (String) value;
                    case "server.port" -> SERVER_PORT = toInt(value);
                    case "server.world_name" -> SERVER_WORLD_NAME = (String) value;
                    case "server.spawn_point" -> SERVER_SPAWN_POS = PosSerializer.deserialize((String) value);
                    // RabbitMQ
                    case "rabbitmq.host" -> RABBITMQ_HOST = (String) value;
                    case "rabbitmq.password" -> RABBITMQ_PASSWORD = (String) value;
                    case "rabbitmq.username" -> RABBITMQ_USERNAME = (String) value;
                    case "rabbitmq.port" -> RABBITMQ_PORT = toInt(value);
                    case "rabbitmq.enabled" -> RABBITMQ_ENABLED = (boolean) value;

                    // Redis
                    case "redis.port" -> REDIS_PORT = toInt(value);
                    case "redis.host" -> REDIS_HOST = (String) value;
                    case "redis.password" -> REDIS_PASSWORD = (String) value;

                    default -> { /*Do nothing*/ }
                }
            } catch (ClassCastException e) {
                Logger.error(STR."Could not import config key: \{key}", e);
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
        // logging
        if (!(System.getenv("LOG_PLAYER_JOINS") == null)) CytosisSettings.LOG_PLAYER_JOINS = Boolean.parseBoolean(System.getenv("LOG_PLAYER_JOINS"));
        if (!(System.getenv("LOG_PLAYER_QUITS") == null)) CytosisSettings.LOG_PLAYER_QUITS = Boolean.parseBoolean(System.getenv("LOG_PLAYER_QUITS"));
        if (!(System.getenv("LOG_PLAYER_COMMANDS") == null)) CytosisSettings.LOG_PLAYER_COMMANDS = Boolean.parseBoolean(System.getenv("LOG_PLAYER_COMMANDS"));
        if (!(System.getenv("LOG_PLAYER_CHAT") == null)) CytosisSettings.LOG_PLAYER_CHAT = Boolean.parseBoolean(System.getenv("LOG_PLAYER_CHAT"));
        if (!(System.getenv("LOG_PLAYER_JOINS") == null))
            LOG_PLAYER_JOINS = Boolean.parseBoolean(System.getenv("LOG_PLAYER_JOINS"));
        if (!(System.getenv("LOG_PLAYER_QUITS") == null))
            LOG_PLAYER_QUITS = Boolean.parseBoolean(System.getenv("LOG_PLAYER_QUITS"));
        if (!(System.getenv("LOG_PLAYER_COMMANDS") == null))
            LOG_PLAYER_COMMANDS = Boolean.parseBoolean(System.getenv("LOG_PLAYER_COMMANDS"));
        if (!(System.getenv("LOG_PLAYER_CHAT") == null))
            LOG_PLAYER_CHAT = Boolean.parseBoolean(System.getenv("LOG_PLAYER_CHAT"));
        // database
        if (!(System.getenv("DATABASE_ENABLED") == null))
            DATABASE_ENABLED = Boolean.parseBoolean(System.getenv("DATABASE_ENABLED"));
        if (!(System.getenv("DATABASE_USER") == null)) DATABASE_USER = System.getenv("DATABASE_USER");
        if (!(System.getenv("DATABASE_PASSWORD") == null)) DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD");
        if (!(System.getenv("DATABASE_HOST") == null)) DATABASE_HOST = System.getenv("DATABASE_HOST");
        if (!(System.getenv("DATABASE_PORT") == null))
            DATABASE_PORT = Integer.parseInt((System.getenv("DATABASE_PORT")));
        if (!(System.getenv("DATABASE_NAME") == null)) DATABASE_NAME = System.getenv("DATABASE_NAME");
        if (!(System.getenv("DATABASE_USE_SSL") == null))
            DATABASE_USE_SSL = Boolean.parseBoolean(System.getenv("DATABASE_USE_SSL"));
        //server
        if (!(System.getenv("SERVER_PROXY_MODE") == null)) CytosisSettings.SERVER_PROXY_MODE = Boolean.parseBoolean(System.getenv("SERVER_PROXY_MODE"));
        if (!(System.getenv("SERVER_SECRET") == null)) CytosisSettings.SERVER_SECRET = System.getenv("SERVER_SECRET");
        if (!(System.getenv("SERVER_PORT") == null)) CytosisSettings.SERVER_PORT = Integer.parseInt(System.getenv("SERVER_PORT"));
        if (!(System.getenv("SERVER_WORLD_NAME") == null)) CytosisSettings.SERVER_WORLD_NAME = System.getenv("SERVER_WORLD_NAME");
        if (!(System.getenv("SERVER_SPAWN_POINT") == null)) CytosisSettings.SERVER_SPAWN_POS = PosSerializer.deserialize(System.getenv("SERVER_SPAWN_POINT"));
        if (!(System.getenv("SERVER_PROXY_MODE") == null))
            SERVER_PROXY_MODE = Boolean.parseBoolean(System.getenv("SERVER_PROXY_MODE"));
        if (!(System.getenv("SERVER_SECRET") == null)) SERVER_SECRET = System.getenv("SERVER_SECRET");
        if (!(System.getenv("SERVER_PORT") == null)) SERVER_PORT = Integer.parseInt(System.getenv("SERVER_PORT"));
        // RabbitMQ
        if (!(System.getenv("RABBITMQ_ENABLED") == null))
            RABBITMQ_ENABLED = Boolean.parseBoolean(System.getenv("RABBITMQ_ENABLED"));
        if (!(System.getenv("RABBITMQ_HOST") == null)) RABBITMQ_HOST = System.getenv("RABBITMQ_HOST");
        if (!(System.getenv("RABBITMQ_PASSWORD") == null)) RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");
        if (!(System.getenv("RABBITMQ_USERNAME") == null)) RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
        if (!(System.getenv("RABBITMQ_PORT") == null)) RABBITMQ_PORT = Integer.parseInt(System.getenv("RABBITMQ_PORT"));

        // redis
        if (!(System.getenv("REDIS_HOST") == null)) REDIS_HOST = System.getenv("REDIS_HOST");
        if (!(System.getenv("REDIS_PORT") == null)) REDIS_PORT = Integer.parseInt(System.getenv("REDIS_PORT"));
        if (!(System.getenv("REDIS_PASSWORD") == null)) REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");
    }

    /**
     * Load settings from command args (System Properties)
     */
    public static void loadCommandArgs() {
        Logger.info("Loading command args!");
        // logging
        if (!(System.getProperty("LOG_PLAYER_JOINS") == null)) CytosisSettings.LOG_PLAYER_JOINS = Boolean.parseBoolean(System.getProperty("LOG_PLAYER_JOINS"));
        if (!(System.getProperty("LOG_PLAYER_QUITS") == null)) CytosisSettings.LOG_PLAYER_QUITS = Boolean.parseBoolean(System.getProperty("LOG_PLAYER_QUITS"));
        if (!(System.getProperty("LOG_PLAYER_COMMANDS") == null)) CytosisSettings.LOG_PLAYER_COMMANDS = Boolean.parseBoolean(System.getProperty("LOG_PLAYER_COMMANDS"));
        if (!(System.getProperty("LOG_PLAYER_CHAT") == null)) CytosisSettings.LOG_PLAYER_CHAT = Boolean.parseBoolean(System.getProperty("LOG_PLAYER_CHAT"));
        if (!(System.getProperty("LOG_PLAYER_JOINS") == null))
            LOG_PLAYER_JOINS = Boolean.parseBoolean(System.getProperty("LOG_PLAYER_JOINS"));
        if (!(System.getProperty("LOG_PLAYER_QUITS") == null))
            LOG_PLAYER_QUITS = Boolean.parseBoolean(System.getProperty("LOG_PLAYER_QUITS"));
        if (!(System.getProperty("LOG_PLAYER_COMMANDS") == null))
            LOG_PLAYER_COMMANDS = Boolean.parseBoolean(System.getProperty("LOG_PLAYER_COMMANDS"));
        if (!(System.getProperty("LOG_PLAYER_CHAT") == null))
            LOG_PLAYER_CHAT = Boolean.parseBoolean(System.getProperty("LOG_PLAYER_CHAT"));
        // database
        if (!(System.getProperty("DATABASE_USER") == null)) DATABASE_USER = System.getProperty("DATABASE_USER");
        if (!(System.getProperty("DATABASE_PASSWORD") == null))
            DATABASE_PASSWORD = System.getProperty("DATABASE_PASSWORD");
        if (!(System.getProperty("DATABASE_HOST") == null)) DATABASE_HOST = System.getProperty("DATABASE_HOST");
        if (!(System.getProperty("DATABASE_PORT") == null))
            DATABASE_PORT = Integer.parseInt((System.getProperty("DATABASE_PORT")));
        if (!(System.getProperty("DATABASE_NAME") == null)) DATABASE_NAME = System.getProperty("DATABASE_NAME");
        if (!(System.getProperty("DATABASE_USE_SSL") == null))
            DATABASE_USE_SSL = Boolean.parseBoolean(System.getProperty("DATABASE_USE_SSL"));
        //server
        if (!(System.getProperty("SERVER_PROXY_MODE") == null)) CytosisSettings.SERVER_PROXY_MODE = Boolean.parseBoolean(System.getProperty("SERVER_PROXY_MODE"));
        if (!(System.getProperty("SERVER_SECRET") == null)) CytosisSettings.SERVER_SECRET = System.getProperty("SERVER_SECRET");
        if (!(System.getProperty("SERVER_PORT") == null)) CytosisSettings.SERVER_PORT = Integer.parseInt(System.getProperty("SERVER_PORT"));
        if (!(System.getProperty("SERVER_WORLD_NAME") == null)) CytosisSettings.SERVER_WORLD_NAME = System.getProperty("SERVER_WORLD_NAME");
        if (!(System.getProperty("SERVER_SPAWN_POINT") == null)) CytosisSettings.SERVER_SPAWN_POS = PosSerializer.deserialize(System.getProperty("SERVER_SPAWN_POINT"));
        if (!(System.getProperty("SERVER_PROXY_MODE") == null))
            SERVER_PROXY_MODE = Boolean.parseBoolean(System.getProperty("SERVER_PROXY_MODE"));
        if (!(System.getProperty("SERVER_SECRET") == null)) SERVER_SECRET = System.getProperty("SERVER_SECRET");
        if (!(System.getProperty("SERVER_PORT") == null))
            SERVER_PORT = Integer.parseInt(System.getProperty("SERVER_PORT"));
        // RabbitMQ
        if (!(System.getProperty("RABBITMQ_ENABLED") == null))
            RABBITMQ_ENABLED = Boolean.parseBoolean(System.getProperty("RABBITMQ_ENABLED"));
        if (!(System.getProperty("RABBITMQ_HOST") == null)) RABBITMQ_HOST = System.getProperty("RABBITMQ_HOST");
        if (!(System.getProperty("RABBITMQ_PASSWORD") == null))
            RABBITMQ_PASSWORD = System.getProperty("RABBITMQ_PASSWORD");
        if (!(System.getProperty("RABBITMQ_USERNAME") == null))
            RABBITMQ_USERNAME = System.getProperty("RABBITMQ_USERNAME");
        if (!(System.getProperty("RABBITMQ_PORT") == null))
            RABBITMQ_PORT = Integer.parseInt(System.getProperty("RABBITMQ_PORT"));

        // redis
        if (!(System.getProperty("REDIS_HOST") == null)) REDIS_HOST = System.getProperty("REDIS_HOST");
        if (!(System.getProperty("REDIS_PORT") == null))
            REDIS_PORT = Integer.parseInt(System.getProperty("REDIS_PORT"));
        if (!(System.getProperty("REDIS_PASSWORD") == null)) REDIS_PASSWORD = System.getProperty("REDIS_PASSWORD");
    }
}