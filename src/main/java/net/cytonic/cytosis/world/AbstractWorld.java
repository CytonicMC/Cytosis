package net.cytonic.cytosis.world;

import java.util.UUID;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;

import net.cytonic.cytosis.particles.ParticleEngine;

@Getter
public abstract class AbstractWorld extends InstanceContainer {

    private final ParticleEngine particleEngine;

    public AbstractWorld(UUID uuid, RegistryKey<DimensionType> dimensionType) {
        super(uuid, dimensionType);

        // make particle engine accessible via a tag or method
        this.particleEngine = new ParticleEngine(this);
        setTag(ParticleEngine.TAG, particleEngine);

        MinecraftServer.getInstanceManager().registerInstance(this);
    }

}
