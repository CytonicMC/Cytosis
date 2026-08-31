package net.cytonic.cytosis.launcher;

import dev.minestomunited.entrypoint.EntryPoint;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.config.EnvOverride;
import net.cytonic.cytosis.utils.Events;

public class CytosisMain {

    static void main(String[] args) {
        EntryPoint.Builder<CytosisServer> builder = EntryPoint.<CytosisServer>builder()
            .server(CytosisServer::new)
            .registerConfig(ServerConfig.class, new ServerConfig("0.0.0.0", 25566))
            .afterSetup(server -> {

                Cytosis.init(server);

                InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
                instance.setGenerator(unit -> unit.modifier().fillHeight(-1, 0, Block.WHITE_STAINED_GLASS));
                instance.setChunkSupplier(LightingChunk::new);

                Events.onAsyncPlayerConfiguration(event -> event.setSpawningInstance(instance));
            }).onConfigLoaded(
                r -> {
                    // Override the already-registered config
                    Cytosis.CONTEXT.registerComponent(
                        EnvOverride.apply("CYTOSIS", CytosisConfig.class, r.get(CytosisConfig.class).orElse(null)));
                });

        Cytosis.applyToBuilder(builder).run(args);
    }
}
