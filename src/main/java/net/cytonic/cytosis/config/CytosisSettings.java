package net.cytonic.cytosis.config;

import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.cytonic.cytosis.utils.PosSerializer;

/**
 * This class is used to store all the cached configuration values.
 */
@Getter
@Setter
@ConfigSerializable
public final class CytosisSettings {

    /**
     * The latest version of the config
     * <p>
     * Bump version by 1 when changing config
     */
    public static final int LATEST_VERSION = 1;

    /**
     * The config version
     */
    @Required
    @Setting("version")
    private int version = LATEST_VERSION;
    /**
     * The database config
     */
    @Required
    @Setting("database")
    private DatabaseConfig databaseConfig = new DatabaseConfig();
    /**
     * The server config
     */
    @Required
    @Setting("server")
    private ServerConfig serverConfig = new ServerConfig();
    /**
     * The redis config
     */
    @Required
    @Setting("redis")
    private RedisConfig redisConfig = new RedisConfig();
    /**
     * The nats config
     */
    @Required
    @Setting("nats")
    private NatsConfig natsConfig = new NatsConfig();

    /**
     * Falls back to loading settings from env and properties
     */
    @PostProcess
    private void postProcess() {
        loadStringFromEnvOrProperty("DATABASE_USER", this.getDatabaseConfig()::setUser);
        loadStringFromEnvOrProperty("DATABASE_PASSWORD", this.getDatabaseConfig()::setPassword);
        loadStringFromEnvOrProperty("DATABASE_HOST", this.getDatabaseConfig()::setHost);
        loadIntFromEnvOrProperty("DATABASE_PORT", this.getDatabaseConfig()::setPort);
        loadStringFromEnvOrProperty("DATABASE_NAME", this.getDatabaseConfig()::setName);
        loadStringFromEnvOrProperty("GLOBAL_DATABASE", this.getDatabaseConfig()::setGlobalDatabase);

        loadStringFromEnvOrProperty("SERVER_SECRET", this.getServerConfig()::setSecret);
        loadIntFromEnvOrProperty("SERVER_PORT", this.getServerConfig()::setPort);
        loadStringFromEnvOrProperty("SERVER_WORLD_NAME", this.getServerConfig()::setWorldName);
        loadStringFromEnvOrProperty("SERVER_SPAWN_POINT",
            string -> this.getServerConfig().setSpawnPos(PosSerializer.deserialize(string)));

        loadStringFromEnvOrProperty("REDIS_HOST", this.getRedisConfig()::setHost);
        loadIntFromEnvOrProperty("REDIS_PORT", this.getRedisConfig()::setPort);
        loadStringFromEnvOrProperty("REDIS_PASSWORD", this.getRedisConfig()::setPassword);

        loadStringFromEnvOrProperty("NATS_USERNAME", this.getNatsConfig()::setUser);
        loadStringFromEnvOrProperty("NATS_HOSTNAME", this.getNatsConfig()::setHost);
        loadIntFromEnvOrProperty("NATS_PORT", this.getNatsConfig()::setPort);
        loadStringFromEnvOrProperty("NATS_PASSWORD", this.getNatsConfig()::setPassword);

    }

    private void loadStringFromEnvOrProperty(String envKey, Consumer<String> setter) {
        String value = System.getenv(envKey);
        if (value == null) value = System.getProperty(envKey);
        if (value != null) setter.accept(value);
    }

    private void loadIntFromEnvOrProperty(String envKey, Consumer<Integer> setter) {
        String value = System.getenv(envKey);
        if (value == null) value = System.getProperty(envKey);
        if (value != null) setter.accept(Integer.valueOf(value));
    }

    @Getter
    @Setter
    @ConfigSerializable
    public static class DatabaseConfig {

        /**
         * Database username
         */
        @Required
        @Setting("user")
        private String user = "cytonic";
        /**
         * Database password
         */
        @Required
        @Setting("password")
        private String password = "password";
        /**
         * Hostname of the database server
         */
        @Setting("host")
        @Required
        private String host = "localhost";
        /**
         * Database port
         */
        @Setting("port")
        @Required
        private int port = 3306;
        /**
         * Name of the database to use
         */
        @Setting("name")
        @Required
        private String name = "cytonic";
        /**
         * Name of the database to use
         */
        @Required
        @Setting("global_name")
        private String globalDatabase = "cytonic_global";
    }

    @Getter
    @Setter
    @ConfigSerializable
    public static class ServerConfig {

        /**
         * The velocity forwarding secret
         */
        @Required
        @Setting("secret")
        private String secret = "cannot-be-empty";
        /**
         * The port to start on
         */
        @Required
        @Setting("port")
        private int port = 25565;
        /**
         * The name of the world to load from the database
         */
        @Required
        @Setting("world_name")
        private String worldName = "world";
        /**
         * The pos for players to spawn at
         */
        @Required
        @Setting("spawn_point")
        private Pos spawnPos = new Pos(0, 1, 0);
    }

    @Getter
    @Setter
    @ConfigSerializable
    public static class RedisConfig {

        /**
         * The redis hostname
         */
        @Required
        @Setting("host")
        private String host = "localhost";
        /**
         * The redis port
         */
        @Required
        @Setting("port")
        private int port = 6379;
        /**
         * The redis password
         */
        @Required
        @Setting("password")
        private String password = "password";
    }

    @Getter
    @Setter
    @ConfigSerializable
    public static class NatsConfig {

        /**
         * The nats hostname
         */
        @Required
        @Setting("host")
        private String host = "localhost";
        /**
         * The nats port
         */
        @Required
        @Setting("port")
        private int port = 4222;
        /**
         * The nats password
         */
        @Required
        @Setting("password")
        private String password = "password";
        /**
         * The nats username
         */
        @Required
        @Setting("username")
        private String user = "cytonic";
    }
}