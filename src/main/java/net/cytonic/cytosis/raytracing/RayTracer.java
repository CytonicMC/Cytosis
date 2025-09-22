package net.cytonic.cytosis.raytracing;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.collision.SweepResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import net.cytonic.cytosis.utils.Utils;

@SuppressWarnings("unused")
public class RayTracer {

    private static final double RAY_BOX_SIZE = 0.001;
    private static final double EPSILON = 1e-8;

    private static Field sweepResultResField;

    static {
        try {
            sweepResultResField = SweepResult.class.getDeclaredField("res");
            sweepResultResField.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
    }

    // Public convenience methods
    public static Optional<RayTraceResult> rayTraceFromEntity(Entity entity, double range, boolean ignoreFluids) {
        Pos eyePosition = entity.getPosition().add(0, entity.getEyeHeight(), 0);
        Vec direction = entity.getPosition().direction();
        return rayTrace(entity.getInstance(), eyePosition, direction, range, ignoreFluids, entity);
    }

    public static Optional<RayTraceResult> rayTrace(Instance instance, Point origin, Vec direction, double range,
        boolean ignoreFluids, Entity... exclude) {
        Vec normalizedDirection = direction.normalize();

        RayTraceResult blockResult = rayTraceBlocks(instance, origin, normalizedDirection, range, ignoreFluids);
        RayTraceResult entityResult = rayTraceEntities(instance, origin, normalizedDirection, range, exclude);

        return Optional.ofNullable(getClosestResult(origin, blockResult, entityResult));
    }

    public static Optional<RayTraceResult> rayTraceBlocks(Instance instance, Vec direction, Point origin, double range,
        boolean ignoreFluids) {
        return Optional.ofNullable(rayTraceBlocks(instance, origin, direction.normalize(), range, ignoreFluids));
    }

    /**
     * Ray trace against blocks using DDA algorithm with collision shape support
     */
    private static RayTraceResult rayTraceBlocks(Instance instance, Point origin, Vec direction, double range,
        boolean ignoreFluids) {
        DdaRayTracer ddaTracer = new DdaRayTracer(origin, direction, range);
        RayTraceResult closestResult = null;
        double closestDistance = Double.MAX_VALUE;

        while (ddaTracer.hasNext()) {
            DdaRayTracer.VoxelPosition voxel = ddaTracer.next();
            Block block = instance.getBlock(voxel.x, voxel.y, voxel.z);

            if (shouldCheckBlock(block, ignoreFluids)) {
                RayTraceResult hitResult = checkBlockCollision(origin, direction, voxel, block, range);

                if (hitResult != null) {
                    double distance = origin.distance(hitResult.hitPosition());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestResult = hitResult;
                    }
                }
            }
        }

        return closestResult;
    }

    public static Optional<RayTraceResult> rayTraceEntities(Instance instance, Vec direction, Point origin,
        double range, Entity... ignore) {
        return Optional.ofNullable(rayTraceEntities(instance, origin, direction.normalize(), range, ignore));
    }

    /**
     * Ray trace against entities using bounding box intersection
     */
    private static RayTraceResult rayTraceEntities(Instance instance, Point origin, Vec direction, double range,
        Entity... excludeEntities) {
        Set<Entity> entities = instance.getEntities();
        List<Entity> toExclude = Utils.list(excludeEntities);

        RayTraceResult closestResult = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            if (toExclude.contains(entity)) {
                continue;
            }

            Optional<Pos> intersection = rayEntityIntersection(origin, direction, entity, range);

            if (intersection.isPresent()) {
                double distance = origin.distance(intersection.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestResult = new RayTraceResult(intersection.get(), entity, null);
                }
            }
        }

        return closestResult;
    }

    private static RayTraceResult getClosestResult(Point origin, RayTraceResult... results) {
        RayTraceResult closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (RayTraceResult result : results) {
            if (result != null) {
                double distance = origin.distance(result.hitPosition());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = result;
                }
            }
        }

        return closest;
    }

    // Helper methods
    private static boolean shouldCheckBlock(Block block, boolean ignoreFluids) {
        return !block.isAir() && (!ignoreFluids || !isFluid(block));
    }

    /**
     * Check ray intersection with a block's collision shape
     */
    private static RayTraceResult checkBlockCollision(Point origin, Vec direction, DdaRayTracer.VoxelPosition voxel,
        Block block, double maxRange) {
        Shape collisionShape = block.registry().collisionShape();

        BoundingBox rayBox = new BoundingBox(RAY_BOX_SIZE, RAY_BOX_SIZE, RAY_BOX_SIZE);
        Point blockWorldPos = new Pos(voxel.x, voxel.y, voxel.z);
        SweepResult sweepResult = createEmptySweepResult();
        Point rayEnd = origin.add(direction.mul(maxRange));

        boolean hit = collisionShape.intersectBoxSwept(origin, rayEnd, blockWorldPos, rayBox, sweepResult);
        double resolution = getSweepResultResolution(sweepResult);

        if (hit && isValidResolution(resolution)) {
            Point hitPoint = origin.add(direction.mul(maxRange * resolution));
            return new RayTraceResult(hitPoint.asPos(), null, block);
        }

        return null;
    }

    /**
     * Check ray intersection with an entity
     */
    private static Optional<Pos> rayEntityIntersection(Point origin, Vec direction, Entity entity, double range) {
        BoundingBox entityBox = createWorldSpaceBoundingBox(entity);
        return rayBoxIntersection(origin, direction, entityBox, range);
    }

    private static boolean isFluid(Block block) {
        return block == Block.WATER || block == Block.LAVA;
    }

    @SuppressWarnings("all")
    private static SweepResult createEmptySweepResult() {
        return new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Get the resolution from SweepResult using cached reflection
     */
    private static double getSweepResultResolution(SweepResult result) {
        if (sweepResultResField == null) {
            return Double.MAX_VALUE;
        }

        try {
            return sweepResultResField.getDouble(result);
        } catch (IllegalAccessException e) {
            return Double.MAX_VALUE;
        }
    }

    private static boolean isValidResolution(double resolution) {
        return resolution >= 0 && resolution <= 1.0;
    }

    /**
     * Create world-space bounding box for an entity
     */
    private static BoundingBox createWorldSpaceBoundingBox(Entity entity) {
        BoundingBox boundingBox = entity.getBoundingBox();
        Pos entityPos = entity.getPosition();

        return new BoundingBox(boundingBox.width(), boundingBox.height(), boundingBox.depth(),
            new Pos(entityPos.x() + boundingBox.relativeStart()
                .x(), entityPos.y() + boundingBox.relativeStart()
                .y(), entityPos.z() + boundingBox.relativeStart()
                .z()));
    }

    /**
     * Calculate ray-box intersection using the slab method
     */
    private static Optional<Pos> rayBoxIntersection(Point origin, Vec direction, BoundingBox box, double maxDistance) {
        Pos boxCenter = box.relativeStart().add(box.relativeEnd()).mul(0.5).asPos();
        Pos boxMin = boxCenter.sub(box.width() / 2, box.height() / 2, box.depth() / 2);
        Pos boxMax = boxMin.add(box.width(), box.height(), box.depth());

        double min = 0.0;
        double max = maxDistance;

        // Check each axis (X, Y, Z)
        double[] origins = {origin.x(), origin.y(), origin.z()};
        double[] directions = {direction.x(), direction.y(), direction.z()};
        double[] mins = {boxMin.x(), boxMin.y(), boxMin.z()};
        double[] maxes = {boxMax.x(), boxMax.y(), boxMax.z()};

        for (int i = 0; i < 3; i++) {
            if (Math.abs(directions[i]) < EPSILON) {
                if (origins[i] < mins[i] || origins[i] > maxes[i]) {
                    return Optional.empty();
                }
            } else {
                double t1 = (mins[i] - origins[i]) / directions[i];
                double t2 = (maxes[i] - origins[i]) / directions[i];

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                min = Math.max(min, t1);
                max = Math.min(max, t2);

                if (min > max) {
                    return Optional.empty();
                }
            }
        }

        double t = min > 0 ? min : max;
        if (t >= 0 && t <= maxDistance) {
            Pos hitPos = origin.asPos().add(direction.mul(t));
            return Optional.of(hitPos);
        }

        return Optional.empty();
    }

    /**
     * Encapsulates DDA ray tracing logic for cleaner code
     */
    private static class DdaRayTracer {

        private final double deltaDistX, deltaDistY, deltaDistZ;
        private final int stepX, stepY, stepZ;
        private final double maxRange;
        private double sideDistX, sideDistY, sideDistZ;
        private int voxelX, voxelY, voxelZ;
        private double totalDistance;

        public DdaRayTracer(Point origin, Vec direction, double maxRange) {
            this.maxRange = maxRange;

            double dx = direction.x();
            double dy = direction.y();
            double dz = direction.z();

            voxelX = (int) Math.floor(origin.x());
            voxelY = (int) Math.floor(origin.y());
            voxelZ = (int) Math.floor(origin.z());

            stepX = dx > 0 ? 1 : -1;
            stepY = dy > 0 ? 1 : -1;
            stepZ = dz > 0 ? 1 : -1;

            deltaDistX = Math.abs(1.0 / dx);
            deltaDistY = Math.abs(1.0 / dy);
            deltaDistZ = Math.abs(1.0 / dz);

            double x = origin.x();
            double y = origin.y();
            double z = origin.z();

            sideDistX = dx < 0 ? (x - voxelX) * deltaDistX : (voxelX + 1.0 - x) * deltaDistX;
            sideDistY = dy < 0 ? (y - voxelY) * deltaDistY : (voxelY + 1.0 - y) * deltaDistY;
            sideDistZ = dz < 0 ? (z - voxelZ) * deltaDistZ : (voxelZ + 1.0 - z) * deltaDistZ;
        }

        public boolean hasNext() {
            return totalDistance < maxRange;
        }

        public VoxelPosition next() {
            VoxelPosition current = new VoxelPosition(voxelX, voxelY, voxelZ);

            // Move to next voxel
            if (sideDistX < sideDistY && sideDistX < sideDistZ) {
                sideDistX += deltaDistX;
                voxelX += stepX;
                totalDistance = sideDistX;
            } else if (sideDistY < sideDistZ) {
                sideDistY += deltaDistY;
                voxelY += stepY;
                totalDistance = sideDistY;
            } else {
                sideDistZ += deltaDistZ;
                voxelZ += stepZ;
                totalDistance = sideDistZ;
            }

            return current;
        }

        public record VoxelPosition(int x, int y, int z) {

        }
    }
}