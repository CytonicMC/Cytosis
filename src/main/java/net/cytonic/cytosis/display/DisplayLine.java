package net.cytonic.cytosis.display;

import java.util.concurrent.CompletableFuture;

import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DisplayLine extends Entity {

    private final Pos spawnPosition;

    public DisplayLine(Point p1, Point p2, Block block, @Nullable Color glowcolor) {
        super(EntityType.BLOCK_DISPLAY);
        double distance = p1.distance(p2);

        Vec from = new Vec(1, 0, 0);
        Vec to = p2.sub(p1).asVec().normalize();
        Quaternionf angle = QuaternionUtils.compose(from, to);

        spawnPosition = p1.asPos();

        editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setHasNoGravity(true);
            meta.setScale(new Vec(distance, 0.0425, 0.0425));
            meta.setLeftRotation(QuaternionUtils.decompose(angle));
            meta.setBlockState(block);
            if (glowcolor != null) {
                meta.setGlowColorOverride(glowcolor.asRGB());
                meta.setHasGlowingEffect(true);
            }
        });
    }

    @Override
    public @NonNull CompletableFuture<Void> setInstance(@NonNull Instance instance) {
        return super.setInstance(instance, spawnPosition);
    }
}
