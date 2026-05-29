package net.cytonic.cytosis.config;

import dev.minestomunited.entrypoint.config.Config;
import dev.minestomunited.entrypoint.config.ConfigFile;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

@ConfigFile("metrics")
public record MetricsConfig(
    boolean enabled,
    @Nullable String host,
    int port
) implements Config {

    public static final Codec<MetricsConfig> CODEC = StructCodec.struct(
        "enabled", Codec.BOOLEAN, MetricsConfig::enabled,
        "host", Codec.STRING, MetricsConfig::host,
        "port", Codec.INT, MetricsConfig::port,
        MetricsConfig::new
    );
}
