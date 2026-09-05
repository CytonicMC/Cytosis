package net.cytonic.cytosis.config;

import dev.minestomunited.entrypoint.config.ServerConfig;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

/**
 * Missing Codec to allow for ServerConfig to be modified with a Codec config format.
 */
public class ServerConfigCodec {

    public static final Codec<ServerConfig> CODEC = StructCodec.struct(
        "host", Codec.STRING, ServerConfig::host,
        "port", Codec.INT, ServerConfig::port,
        ServerConfig::new
    );

}
