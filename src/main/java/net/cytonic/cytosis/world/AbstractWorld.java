package net.cytonic.cytosis.world;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.WorldManager;

public abstract class AbstractWorld extends InstanceContainer {

    public static final Tag<Key> MAP_ID;

    static {
        Tag<Key> tag = Tag.Transient("cytosis:map_id");
        MAP_ID = tag.defaultValue(Key.key("cytosis:default"));
    }

    public AbstractWorld(UUID uuid, RegistryKey<DimensionType> dimensionType) {
        this(uuid, dimensionType, true);
    }

    public AbstractWorld(UUID uuid, RegistryKey<DimensionType> dimensionType, boolean autoRegister) {
        super(uuid, dimensionType);
        if (autoRegister) {
            MinecraftServer.getInstanceManager().registerInstance(this);
        }
    }

    protected CompletableFuture<Void> loadPolarWorld(Key map) {
        if (map.equals(Key.key("cytosis:default"))) { // reserved for empty maps
            setGenerator(unit -> unit.modifier().fillHeight(-1, 0, Block.WHITE_STAINED_GLASS));
            setChunkSupplier(LightingChunk::new);
            setTag(MAP_ID, map);
            return CompletableFuture.completedFuture(null);
        }
        return Cytosis.get(WorldManager.class).loadWorld(map).thenAccept(pw -> {
            setChunkLoader(new PolarLoader(pw));
            setTag(MAP_ID, map);
        });
    }
}
