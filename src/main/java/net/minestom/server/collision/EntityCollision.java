package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class EntityCollision {
    static List<EntityCollisionResult> checkCollision(
            EntityTracker entityTracker,
            BoundingBox boundingBox, Point point, Vec entityVelocity,
            double extendRadius, Function<Entity, Boolean> entityFilter, @Nullable PhysicsResult physicsResult
    ) {
        final double minimumRes = physicsResult != null ? physicsResult.res().res : Double.MAX_VALUE;
        // Approximation of the largest candidate bounding box reach, measured from its position.
        final double maxDistance = Math.cbrt(boundingBox.height() * boundingBox.height()
                + boundingBox.depth() / 2 * boundingBox.depth() / 2
                + boundingBox.width() / 2 * boundingBox.width() / 2);
        final double range = extendRadius + maxDistance + entityVelocity.length();
        final double rangeSquared = range * range;

        final double px = point.x(), py = point.y(), pz = point.z();
        final double vx = entityVelocity.x(), vy = entityVelocity.y(), vz = entityVelocity.z();
        // Mover world bounds, and the same epsilon-shrunk (relative to `point`) for the overlap test.
        final double moMinX = px + boundingBox.minX(), moMaxX = px + boundingBox.maxX();
        final double moMinY = py + boundingBox.minY(), moMaxY = py + boundingBox.maxY();
        final double moMinZ = pz + boundingBox.minZ(), moMaxZ = pz + boundingBox.maxZ();
        final double shrunkMinX = boundingBox.minX() + Vec.EPSILON / 2;
        final double shrunkMaxX = boundingBox.maxX() - Vec.EPSILON / 2;
        final double shrunkMinY = boundingBox.minY() + Vec.EPSILON / 2;
        final double shrunkMaxY = boundingBox.maxY() - Vec.EPSILON / 2;
        final double shrunkMinZ = boundingBox.minZ() + Vec.EPSILON / 2;
        final double shrunkMaxZ = boundingBox.maxZ() - Vec.EPSILON / 2;

        final Pos pointPos = point.asPos();
        final List<EntityCollisionResult> result = new ArrayList<>();
        final SweepResult sweepResult = new SweepResult(minimumRes, 0, 0, 0, null, 0, 0, 0, 0, 0, 0);

        // Broad phase: scan the chunks covering the search sphere and filter by distance ourselves,
        // avoiding the per-entity tracker-entry lookup of EntityTracker#nearbyEntities.
        final int chunkRange = (int) (range / 16) + 1;
        entityTracker.nearbyEntitiesByChunkRange(point, chunkRange, EntityTracker.Target.ENTITIES, e -> {
            final Pos pos = e.getPosition();
            final double dx = pos.x() - px, dy = pos.y() - py, dz = pos.z() - pz;
            if (dx * dx + dy * dy + dz * dz > rangeSquared) return;
            if (!entityFilter.apply(e)) return;
            if (!e.hasEntityCollision()) return;
            final BoundingBox bb = e.getBoundingBox();

            // Already overlapping: math can't be done, return the entity at the current point
            if (bb.minX() + dx <= shrunkMaxX && bb.maxX() + dx >= shrunkMinX
                    && bb.minY() + dy <= shrunkMaxY && bb.maxY() + dy >= shrunkMinY
                    && bb.minZ() + dz <= shrunkMaxZ && bb.maxZ() + dz >= shrunkMinZ) {
                result.add(new EntityCollisionResult(pointPos, e, Vec.ZERO, 0));
                return;
            }

            // Swept collision check
            sweepResult.res = minimumRes;
            if (RayUtils.sweep(moMinX, moMinY, moMinZ, moMaxX, moMaxY, moMaxZ, vx, vy, vz,
                    bb.minX() + pos.x(), bb.minY() + pos.y(), bb.minZ() + pos.z(),
                    bb.maxX() + pos.x(), bb.maxY() + pos.y(), bb.maxZ() + pos.z(), sweepResult)
                    && sweepResult.res < 1) {
                final double res = sweepResult.res;
                final double cx = px + vx * res, cy = py + vy * res, cz = pz + vz * res;
                result.add(new EntityCollisionResult(pointPos.withCoord(cx, cy, cz), e, new Vec(cx, cy, cz), res));
            }
        });

        return result;
    }
}
