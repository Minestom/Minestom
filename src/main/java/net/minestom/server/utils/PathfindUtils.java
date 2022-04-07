package net.minestom.server.utils;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.entity.pathfinding.engine.astar.AStarEngine;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PathfindUtils {

    public static boolean isBlocked(
            @NotNull Point point,
            @NotNull BoundingBox box,
            @NotNull Block.Getter getter,
            double entityPadding
    ) {
        Point relStart = box.relativeStart();
        Point relEnd = box.relativeEnd();
        relStart = relStart.mul(2, 0, 2).sub(entityPadding, 0, entityPadding);
        relEnd = relEnd.mul(2, 0, 2).add(entityPadding, 0, entityPadding);
        return getter.getBlock(point.add(relStart.x(), relStart.y(), relStart.z())).isSolid() ||
                getter.getBlock(point.add(relStart.x(), relStart.y(), relEnd.z())).isSolid() ||
                getter.getBlock(point.add(relStart.x(), relEnd.y(), relStart.z())).isSolid() ||
                getter.getBlock(point.add(relStart.x(), relEnd.y(), relEnd.z())).isSolid() ||
                getter.getBlock(point.add(relEnd.x(), relStart.y(), relStart.z())).isSolid() ||
                getter.getBlock(point.add(relEnd.x(), relStart.y(), relEnd.z())).isSolid() ||
                getter.getBlock(point.add(relEnd.x(), relEnd.y(), relStart.z())).isSolid() ||
                getter.getBlock(point.add(relEnd.x(), relEnd.y(), relEnd.z())).isSolid();
    }

    public static @NotNull PathfindingEngine getEngine(@NotNull Entity entity) {
        // TODO: Move this to an actual api
        Instance instance = entity.getInstance();
        Objects.requireNonNull(instance, "The navigator must be in an instance while pathfinding.");
        return new AStarEngine(instance, 0.05);
    }

    public static boolean isTouching(BoundingBox box, Point boxPos, Point target, double delta) {
        Point start = boxPos.add(box.relativeStart());
        Point end = boxPos.add(box.relativeEnd());
        return !(target.x() < start.x() - delta ||
                target.x() > end.x() + delta ||
                target.y() < start.y() - delta ||
                target.y() > end.y() + delta ||
                target.z() < start.z() - delta ||
                target.z() > end.z() + delta);
    }

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied, so the entity will fall if it's not on a solid block.
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction the targeted position
     * @param speed     define how far the entity will move
     * @return true if the entity moved, false otherwise
     */
    public static boolean moveEntity(@NotNull Entity entity, @NotNull Point direction, double speed, boolean moveUpwards) {
        final Pos position = entity.getPosition();
        final double dx = direction.x() - position.x();
        final double dy = direction.y() - position.y();
        final double dz = direction.z() - position.z();
        // the purpose of these few lines is to slow down entities when they reach their destination
        final double distSquared = dx * dx + dy * dy + dz * dz;
        if (speed > distSquared) {
            speed = distSquared;
        }
        final double radians = Math.atan2(dz, dx);
        final double speedX = Math.cos(radians) * speed;
        final double speedY = moveUpwards ? dy * speed : 0;
        final double speedZ = Math.sin(radians) * speed;
        final float yaw = PositionUtils.getLookYaw(dx, dz);
        final float pitch = PositionUtils.getLookPitch(dx, dy, dz);
        // Prevent ghosting
        final var physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        if (position.equals(physicsResult.newPosition())) {
            return false;
        }
            entity.refreshPosition(physicsResult.newPosition().withView(yaw, pitch));
        return true;
    }

    public static void jump(Entity entity, double jumpHeight) {
        entity.setVelocity(new Vec(0, jumpHeight * 2.5, 0));
    }
}
