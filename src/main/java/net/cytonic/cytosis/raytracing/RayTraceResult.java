package net.cytonic.cytosis.raytracing;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RayTraceResult(@NotNull Pos hitPosition, @Nullable Entity entity, @Nullable Block block) {
    public boolean hitEntity() {
        return entity != null;
    }

    public boolean hitBlock() {
        return block != null;
    }
}
