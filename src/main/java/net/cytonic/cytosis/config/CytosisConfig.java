package net.cytonic.cytosis.config;

import dev.minestomunited.common.config.Config;
import dev.minestomunited.common.config.ConfigFile;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.environments.Environment;

@ConfigFile("config")
public record CytosisConfig(
    DatabaseConfig database,
    RedisConfig redis,
    NatsConfig nats,
    GarageConfig garage,
    MetricsConfig metrics,
    Environment environment,
    String secret,
    int port,
    boolean standalone
) implements Config {

    public static final Codec<CytosisConfig> CODEC = StructCodec.struct(
        "database", DatabaseConfig.CODEC, CytosisConfig::database,
        "redis", RedisConfig.CODEC, CytosisConfig::redis,
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
        String username,
        String password,
        String host,
        int port,
        String database,
        String globalDatabase
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
        String host,
        int port,
        String password
    ) {

        public static final Codec<RedisConfig> CODEC = StructCodec.struct(
            "host", Codec.STRING, RedisConfig::host,
            "port", Codec.INT, RedisConfig::port,
            "password", Codec.STRING, RedisConfig::password,
            RedisConfig::new
        );
    }

    public record NatsConfig(
        String host,
        int port,
        String username,
        String password
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
        String host,
        int port,
        String username,
        String password
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
        boolean enabled,
        @Nullable String host,
        int port
    ) {

        public static final Codec<MetricsConfig> CODEC = StructCodec.struct(
            "enabled", Codec.BOOLEAN, MetricsConfig::enabled,
            "host", Codec.STRING, MetricsConfig::host,
            "port", Codec.INT, MetricsConfig::port,
            MetricsConfig::new
        );
    }
}
