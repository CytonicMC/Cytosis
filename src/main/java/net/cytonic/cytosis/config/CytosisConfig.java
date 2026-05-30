package net.cytonic.cytosis.config;

import dev.minestomunited.entrypoint.config.Config;
import dev.minestomunited.entrypoint.config.ConfigFile;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import net.cytonic.cytosis.environments.Environment;

@ConfigFile("config")
public record CytosisConfig(
    DatabaseConfig database,
    RedisConfig redis,
    NatsConfig nats,
    MinioConfig minio,
    Environment environment,
    String secret,
    int port
) implements Config {

    public static final Codec<CytosisConfig> CODEC = StructCodec.struct(
        "database", DatabaseConfig.CODEC, CytosisConfig::database,
        "redis", RedisConfig.CODEC, CytosisConfig::redis,
        "nats", NatsConfig.CODEC, CytosisConfig::nats,
        "minio", MinioConfig.CODEC, CytosisConfig::minio,
        "environment", Environment.CODEC, CytosisConfig::environment,
        "secret", Codec.STRING.optional(), CytosisConfig::secret,
        "port", Codec.INT, CytosisConfig::port,
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

    public record MinioConfig(
        String host,
        int port,
        String username,
        String password
    ) {

        public static final Codec<MinioConfig> CODEC = StructCodec.struct(
            "host", Codec.STRING, MinioConfig::host,
            "port", Codec.INT, MinioConfig::port,
            "username", Codec.STRING, MinioConfig::username,
            "password", Codec.STRING, MinioConfig::password,
            MinioConfig::new
        );
    }
}
