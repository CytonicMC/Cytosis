package net.cytonic.cytosis.world;

import java.util.UUID;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;

public abstract class AbstractWorld extends InstanceContainer {
    public AbstractWorld(UUID uuid, RegistryKey<DimensionType> dimensionType) {
        super(uuid, dimensionType);
    }
}
