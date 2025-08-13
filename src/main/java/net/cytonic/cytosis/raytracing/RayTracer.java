package net.cytonic.cytosis.raytracing;

import net.cytonic.cytosis.utils.Utils;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * TODO: Fix block hitboxes not being taken into account!
 */
public class RayTracer {

    public static Optional<RayTraceResult> rayTrace(Instance instance, Point origin, Vec direction, double range, boolean ignoreFluids, Entity... exclude) {
        // Normalize the direction vector
        Vec normalizedDirection = direction.normalize();

        double closestDistance = Double.MAX_VALUE;
        RayTraceResult closestResult = null;

        // Check for block intersections
        RayTraceResult blockResult = rayTraceBlocks(instance, origin, normalizedDirection, range, ignoreFluids);
        if (blockResult != null) {
            double distance = origin.distance(blockResult.hitPosition());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestResult = blockResult;
            }
        }

        // Check for entity intersections
        RayTraceResult entityResult = rayTraceEntities(instance, origin, normalizedDirection, range, exclude);
        if (entityResult != null) {
            double distance = origin.distance(entityResult.hitPosition());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestResult = entityResult;
            }
        }

        return Optional.ofNullable(closestResult);
    }

    /**
     * Ray trace against blocks using DDA (Digital Differential Analyzer) algorithm
     */
    private static RayTraceResult rayTraceBlocks(Instance instance, Point origin, Vec direction, double range, boolean ignoreFluids) {
        double x = origin.x();
        double y = origin.y();
        double z = origin.z();

        double dx = direction.x();
        double dy = direction.y();
        double dz = direction.z();

        // Current voxel position
        int voxelX = (int) Math.floor(x);
        int voxelY = (int) Math.floor(y);
        int voxelZ = (int) Math.floor(z);

        // Direction to step in each axis (-1 or 1)
        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;
        int stepZ = dz > 0 ? 1 : -1;

        // Calculate delta distances
        double deltaDistX = Math.abs(1.0 / dx);
        double deltaDistY = Math.abs(1.0 / dy);
        double deltaDistZ = Math.abs(1.0 / dz);

        // Calculate step and initial side distances
        double sideDistX, sideDistY, sideDistZ;

        if (dx < 0) {
            sideDistX = (x - voxelX) * deltaDistX;
        } else {
            sideDistX = (voxelX + 1.0 - x) * deltaDistX;
        }

        if (dy < 0) {
            sideDistY = (y - voxelY) * deltaDistY;
        } else {
            sideDistY = (voxelY + 1.0 - y) * deltaDistY;
        }

        if (dz < 0) {
            sideDistZ = (z - voxelZ) * deltaDistZ;
        } else {
            sideDistZ = (voxelZ + 1.0 - z) * deltaDistZ;
        }

        double totalDistance = 0;

        // Perform DDA
        while (totalDistance < range) {
            Block block = instance.getBlock(voxelX, voxelY, voxelZ);

            if (!block.isAir() && (!ignoreFluids || !isFluid(block))) {
                // Calculate exact hit position
                Pos hitPos = calculateBlockHitPosition(origin, direction, voxelX, voxelY, voxelZ);
                return new RayTraceResult(hitPos, null, block);
            }

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
        }

        return null;
    }

    /**
     * Ray trace against entities using bounding box intersection
     */
    /**
     * Ray trace against entities using bounding box intersection
     */
    private static RayTraceResult rayTraceEntities(Instance instance, Point origin, Vec direction, double range, Entity... excludeEntities) {
        Set<Entity> entities = instance.getEntities();
        List<Entity> toExclude = Utils.list(excludeEntities);

        RayTraceResult closestResult = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            // Skip the excluded entity (usually the one doing the ray tracing)
            if (toExclude.contains(entity)) {
                continue;
            }

            BoundingBox boundingBox = entity.getBoundingBox();
            Pos entityPos = entity.getPosition();

            // Create world-space bounding box
            BoundingBox worldBoundingBox = new BoundingBox(
                    boundingBox.width(),
                    boundingBox.height(),
                    boundingBox.depth(),
                    new Pos(
                            entityPos.x() + boundingBox.relativeStart().x(),
                            entityPos.y() + boundingBox.relativeStart().y(),
                            entityPos.z() + boundingBox.relativeStart().z()
                    )
            );

            Optional<Pos> intersection = rayBoxIntersection(origin, direction, worldBoundingBox, range);

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

    /**
     * Calculate ray-box intersection using the slab method
     */
    private static Optional<Pos> rayBoxIntersection(Point origin, Vec direction, BoundingBox box, double maxDistance) {
        Pos boxMin = box.relativeStart().add(box.relativeEnd()).mul(0.5).sub(box.width() / 2, box.height() / 2, box.depth() / 2).asPos();
        Pos boxMax = boxMin.add(box.width(), box.height(), box.depth());

        double tmin = 0.0;
        double tmax = maxDistance;

        // Check X slab
        if (Math.abs(direction.x()) < 1e-8) {
            if (origin.x() < boxMin.x() || origin.x() > boxMax.x()) {
                return Optional.empty();
            }
        } else {
            double t1 = (boxMin.x() - origin.x()) / direction.x();
            double t2 = (boxMax.x() - origin.x()) / direction.x();

            if (t1 > t2) {
                double temp = t1;
                t1 = t2;
                t2 = temp;
            }

            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);

            if (tmin > tmax) {
                return Optional.empty();
            }
        }

        // Check Y slab
        if (Math.abs(direction.y()) < 1e-8) {
            if (origin.y() < boxMin.y() || origin.y() > boxMax.y()) {
                return Optional.empty();
            }
        } else {
            double t1 = (boxMin.y() - origin.y()) / direction.y();
            double t2 = (boxMax.y() - origin.y()) / direction.y();

            if (t1 > t2) {
                double temp = t1;
                t1 = t2;
                t2 = temp;
            }

            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);

            if (tmin > tmax) {
                return Optional.empty();
            }
        }

        // Check Z slab
        if (Math.abs(direction.z()) < 1e-8) {
            if (origin.z() < boxMin.z() || origin.z() > boxMax.z()) {
                return Optional.empty();
            }
        } else {
            double t1 = (boxMin.z() - origin.z()) / direction.z();
            double t2 = (boxMax.z() - origin.z()) / direction.z();

            if (t1 > t2) {
                double temp = t1;
                t1 = t2;
                t2 = temp;
            }

            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);

            if (tmin > tmax) {
                return Optional.empty();
            }
        }

        // Calculate intersection point
        double t = tmin > 0 ? tmin : tmax;
        if (t >= 0 && t <= maxDistance) {
            Pos hitPos = Pos.fromPoint(origin).add(direction.mul(t));
            return Optional.of(hitPos);
        }

        return Optional.empty();
    }

    /**
     * Calculate the exact hit position on a block face
     */
    private static Pos calculateBlockHitPosition(Point origin, Vec direction, int blockX, int blockY, int blockZ) {
        // Simple implementation - returns the point where the ray enters the block
        // You could enhance this to return the exact face intersection point

        double minT = Double.MAX_VALUE;
        Pos hitPos = null;

        // Check intersection with each face of the block
        double[][] faces = {
                {blockX, blockY, blockZ, blockX + 1, blockY + 1, blockZ}, // -Z face
                {blockX, blockY, blockZ + 1, blockX + 1, blockY + 1, blockZ + 1}, // +Z face
                {blockX, blockY, blockZ, blockX + 1, blockY, blockZ + 1}, // -Y face
                {blockX, blockY + 1, blockZ, blockX + 1, blockY + 1, blockZ + 1}, // +Y face
                {blockX, blockY, blockZ, blockX, blockY + 1, blockZ + 1}, // -X face
                {blockX + 1, blockY, blockZ, blockX + 1, blockY + 1, blockZ + 1}  // +X face
        };

        for (double[] face : faces) {
            Optional<Pos> intersection = rayPlaneIntersection(origin, direction, face);
            if (intersection.isPresent()) {
                double t = origin.distance(intersection.get());
                if (t < minT) {
                    minT = t;
                    hitPos = intersection.get();
                }
            }
        }

        return hitPos != null ? hitPos : Pos.fromPoint(origin.add(direction.mul(minT)));
    }

    /**
     * Calculate ray-plane intersection for a rectangular face
     */
    private static Optional<Pos> rayPlaneIntersection(Point origin, Vec direction, double[] face) {
        // This is a simplified implementation
        // In a full implementation, you'd calculate the exact intersection with each face
        double centerX = (face[0] + face[3]) / 2;
        double centerY = (face[1] + face[4]) / 2;
        double centerZ = (face[2] + face[5]) / 2;

        return Optional.of(new Pos(centerX, centerY, centerZ));
    }

    /**
     * Check if a block is a fluid
     */
    private static boolean isFluid(Block block) {
        return block == Block.WATER || block == Block.LAVA;
    }

    /**
     * Convenience method for ray tracing from an entity's eye position
     */
    public static Optional<RayTraceResult> rayTraceFromEntity(Entity entity, double range, boolean ignoreFluids) {
        Pos eyePosition = entity.getPosition().add(0, entity.getEyeHeight(), 0);
        Vec direction = entity.getPosition().direction();

        return rayTrace(entity.getInstance(), eyePosition, direction, range, ignoreFluids, entity);
    }

    /**
     * Ray trace only blocks (ignore entities)
     */
    public static Optional<RayTraceResult> rayTraceBlocks(Instance instance, Vec direction, Point origin, double range, boolean ignoreFluids) {
        RayTraceResult result = rayTraceBlocks(instance, origin, direction.normalize(), range, ignoreFluids);
        return Optional.ofNullable(result);
    }

    /**
     * Ray trace only entities (ignore blocks)
     */
    public static Optional<RayTraceResult> rayTraceEntities(Instance instance, Vec direction, Point origin, double range, Entity... ignore) {
        RayTraceResult result = rayTraceEntities(instance, origin, direction.normalize(), range, ignore);
        return Optional.ofNullable(result);
    }
}
