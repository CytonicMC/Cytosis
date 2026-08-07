package net.cytonic.cytosis.config;

import dev.minestomunited.common.config.Config;
import dev.minestomunited.common.config.ConfigFile;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.environments.Environment;

@ConfigFile("config")
public record CytosisConfig(
    @Env DatabaseConfig database,
    @Env RedisConfig redis,
    @Env MongoConfig mongo,
    @Env NatsConfig nats,
    @Env GarageConfig garage,
    @Env MetricsConfig metrics,
    @Env Environment environment,
    @Env String secret,
    @Env int port,
    @Env boolean standalone
) implements Config {

    public static final Codec<CytosisConfig> CODEC = StructCodec.struct(
        "database", DatabaseConfig.CODEC, CytosisConfig::database,
        "redis", RedisConfig.CODEC, CytosisConfig::redis,
        "mongo", MongoConfig.CODEC, CytosisConfig::mongo,
        "nats", NatsConfig.CODEC, CytosisConfig::nats,
        "garage", GarageConfig.CODEC, CytosisConfig::garage,
        "metrics", MetricsConfig.CODEC.optional(new MetricsConfig(false, null, -1)), CytosisConfig::metrics,
        "environment", Environment.CODEC, CytosisConfig::environment,
        "secret", Codec.STRING.optional(), CytosisConfig::secret,
        "port", Codec.INT, CytosisConfig::port,
        "standalone", Codec.BOOLEAN.optional(false), CytosisConfig::standalone,
        CytosisConfig::new
    );

    public record DatabaseConfig(
        @Env String username,
        @Env String password,
        @Env String host,
        @Env int port,
        @Env String database,
        @Env String globalDatabase
    ) {

        public static final Codec<DatabaseConfig> CODEC = StructCodec.struct(
            "username", Codec.STRING, DatabaseConfig::username,
            "password", Codec.STRING, DatabaseConfig::password,
            "host", Codec.STRING, DatabaseConfig::host,
            "port", Codec.INT, DatabaseConfig::port,
            "database", Codec.STRING, DatabaseConfig::database,
            "global_database", Codec.STRING, DatabaseConfig::globalDatabase,
            DatabaseConfig::new
        );
    }

    public record RedisConfig(
        @Env String host,
        @Env int port,
        @Env String password
    ) {

        public static final Codec<RedisConfig> CODEC = StructCodec.struct(
            "host", Codec.STRING, RedisConfig::host,
            "port", Codec.INT, RedisConfig::port,
            "password", Codec.STRING, RedisConfig::password,
            RedisConfig::new
        );
    }

    public record MongoConfig(
        @Env String user,
        @Env String password,
        @Env String host,
        @Env int port,
        @Env String database) {

        public static final Codec<MongoConfig> CODEC = StructCodec.struct(
            "user", Codec.STRING, MongoConfig::user,
            "password", Codec.STRING, MongoConfig::password,
            "host", Codec.STRING, MongoConfig::host,
            "port", Codec.INT, MongoConfig::port,
            "database", Codec.STRING, MongoConfig::database,
            MongoConfig::new
        );

        public String url() {
            boolean hasCreds = user != null && !user.isBlank();

            String authPart = hasCreds ? user + ":" + (password == null ? "" : password) + "@" : "";

            return "mongodb://" + authPart + host + ":" + port + "?authSource=admin";
        }
    }

    public record NatsConfig(
        @Env String host,
        @Env int port,
        @Env String username,
        @Env String password
    ) {

        public static final Codec<NatsConfig> CODEC = StructCodec.struct(
            "host", Codec.STRING, NatsConfig::host,
            "port", Codec.INT, NatsConfig::port,
            "username", Codec.STRING, NatsConfig::username,
            "password", Codec.STRING, NatsConfig::password,
            NatsConfig::new
        );
    }

    public record GarageConfig(
        @Env String host,
        @Env int port,
        @Env String username,
        @Env String password
    ) {

        public static final Codec<GarageConfig> CODEC = StructCodec.struct(
            "host", Codec.STRING, GarageConfig::host,
            "port", Codec.INT, GarageConfig::port,
            "username", Codec.STRING, GarageConfig::username,
            "password", Codec.STRING, GarageConfig::password,
            GarageConfig::new
        );
    }

    public record MetricsConfig(
        @Env boolean enabled,
        @Env @Nullable String host,
        @Env int port
    ) {

        public static final Codec<MetricsConfig> CODEC = StructCodec.struct(
            "enabled", Codec.BOOLEAN, MetricsConfig::enabled,
            "host", Codec.STRING, MetricsConfig::host,
            "port", Codec.INT, MetricsConfig::port,
            MetricsConfig::new
        );
    }
}
