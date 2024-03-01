package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public interface NodeGenerator {
    /**
     * Gets the walkable nodes.
     * @param instance the instance
     * @param visited the visited nodes
     * @param current the current node
     * @param goal the goal
     * @param boundingBox the bounding box
     * @return the walkable nodes
     */
    Collection<? extends PNode> getWalkable(Instance instance, Set<PNode> visited, PNode current, Point goal, @NotNull BoundingBox boundingBox);

    /**
     * Snap the point to the ground.
     * @param instance
     * @param point
     * @param boundingBox
     * @param maxFall
     * @return
     */
    default Point gravitySnap(Instance instance, Point point, BoundingBox boundingBox, double maxFall) {
        point = new Pos(point.blockX() + 0.5, point.blockY(), point.blockZ() + 0.5);

        Chunk c = instance.getChunkAt(point);
        if (c == null) return null;

        for (int axis = 1; axis <= maxFall; ++axis) {
            var iterator = boundingBox.getBlocks(point, BoundingBox.AxisMask.Y, -axis);

            while (iterator.hasNext()) {
                var block = iterator.next();

                if (instance.getBlock(block, Block.Getter.Condition.TYPE).isSolid()) {
                    return point.withY(block.blockY() + 1);
                }
            }
        }

        return point.withY(point.y() - maxFall);
    }

    /**
     * Check if we can move directly from one point to another
     * @param instance
     * @param start
     * @param end
     * @param boundingBox
     * @return
     */
    default boolean canMoveTowards(Instance instance, Point start, Point end, BoundingBox boundingBox) {
        Point diff = end.sub(start);
        PhysicsResult res = CollisionUtils.handlePhysics(instance, instance.getChunkAt(start), boundingBox, Pos.fromPoint(start), Vec.fromPoint(diff), null, false);
        return !res.collisionZ() && !res.collisionY() && !res.collisionX();
    }

    /**
     * Check if the point is invalid
     * @param instance
     * @param point
     * @param boundingBox
     * @return
     */
    default boolean pointInvalid(Instance instance, Point point, BoundingBox boundingBox) {
        var iterator = boundingBox.getBlocks(point);
        while (iterator.hasNext()) {
            var block = iterator.next();
            if (instance.getBlock(block, Block.Getter.Condition.TYPE).isSolid()) {
                return true;
            }
        }

        return false;
    }

}
