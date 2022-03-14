package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

import java.util.Collection;

public class EntityCollisionUtils {
    final static Vec power = new Vec(0.07, 0.07, 0.07);

    public static Vec calculateEntityCollisions(Entity entity) {
        BoundingBox bb = entity.getBoundingBox();
        double bbFurthestCorner = Math.sqrt(bb.depth() + bb.height() + bb.width());

        // These entities don't have collisions
        if (entity.getEntityType() == EntityType.ARMOR_STAND
                || entity.getEntityType() == EntityType.VILLAGER
                || entity.getEntityType() == EntityType.AREA_EFFECT_CLOUD
                || entity.getEntityType() == EntityType.LIGHTNING_BOLT
                || entity.getEntityType() == EntityType.ARROW
                || entity.getEntityType() == EntityType.SPECTRAL_ARROW
                || !entity.getHasCollisions())
            return Vec.ZERO;

        if (entity.getInstance() == null)
            return Vec.ZERO;

        Vec vecAcc = Vec.ZERO;

        Collection<Entity> foundNearby = entity.getInstance().getNearbyEntities(entity.getPosition(), bbFurthestCorner)
                .stream()
                .filter(e ->
                        e.getEntityType() != EntityType.ARMOR_STAND &&
                                e.getEntityType() != EntityType.AREA_EFFECT_CLOUD &&
                                e.getEntityType() != EntityType.LIGHTNING_BOLT &&
                                e.getEntityType() != EntityType.ARROW &&
                                e.getEntityType() != EntityType.SPECTRAL_ARROW &&
                                e.getHasCollisions()
                ).toList();

        for (Entity nearby : foundNearby) {
            if (nearby == entity)
                continue;

            BoundingBox collisionCheckBB = nearby.getBoundingBox();
            if (collisionCheckBB.intersectBox(nearby.getPosition().sub(entity.getPosition()), bb)) {
                // Find shortest resolution to collision by calculating the two faces with the shortest distance
                // Only solve collision for X and Z. Y doesn't matter because gravity
                double overlapX, overlapZ;
                double currentDistanceX, currentDistanceZ;

                // X
                {
                    // Nearby left of entity
                    currentDistanceX = entity.getPosition().x() - nearby.getPosition().x();

                    // Min distance without overlap
                    double minDistance = collisionCheckBB.width() / 2 + bb.width() / 2;

                    // Could be used to calculate how much of a movement to make
                    overlapX = minDistance - Math.abs(currentDistanceX);
                }

                // If y is implemented, min distance calculation isn't h1 / 2 + h2 / 2, because entity position is from bottom of bounding box, not centre

                // Z
                {
                    // Nearby left of entity
                    currentDistanceZ = entity.getPosition().z() - nearby.getPosition().z();

                    // Min distance without overlap
                    double minDistance = collisionCheckBB.depth() / 2 + bb.depth() / 2;

                    // Could be used to calculate how much of a movement to make
                    overlapZ = minDistance - Math.abs(currentDistanceZ);
                }

                if (Math.abs(currentDistanceX) > Math.abs(currentDistanceZ)) {
                    // X axis shorter
                    if (currentDistanceX > 0) {
                        vecAcc = vecAcc.add(new Vec(1, 0, 0));
                    } else {
                        vecAcc = vecAcc.add(new Vec(-1, 0, 0));
                    }
                } else {
                    if (currentDistanceZ > 0) {
                        vecAcc = vecAcc.add(new Vec(0, 0, 1));
                    } else {
                        vecAcc = vecAcc.add(new Vec(0, 0, -1));
                    }
                }
            }
        }

        if (vecAcc.isZero())
            return vecAcc;
        else
            return vecAcc.normalize().mul(power);
    }
}