package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;

public interface NodeGenerator {
    /**
     * Gets the walkable nodes.
     *
     * @param getter      the instance
     * @param visited     the visited nodes
     * @param current     the current node
     * @param goal        the goal
     * @param boundingBox the bounding box
     * @return the walkable nodes
     */
    Collection<? extends PNode> getWalkable(Block.Getter getter, Set<PNode> visited,
                                                     PNode current, Point goal, BoundingBox boundingBox);

    /**
     * @return snap start and end points to the ground
     */
    boolean hasGravitySnap();

    /**
     * Snap point to the ground
     *
     * @param getter      the block getter
     * @param pointX      the x coordinate
     * @param pointY      the y coordinate
     * @param pointZ      the z coordinate
     * @param boundingBox the bounding box
     * @param maxFall     the maximum fall distance
     * @return the snapped y coordinate. Empty if the snap point is not found
     */
    OptionalDouble gravitySnap(Block.Getter getter, double pointX, double pointY, double pointZ,
                                        BoundingBox boundingBox, double maxFall);

    /**
     * Check if we can move directly from one point to another
     *
     * @param getter
     * @param start
     * @param end
     * @param boundingBox
     * @return true if we can move directly from start to end
     */
    default boolean canMoveTowards(Block.Getter getter, Point start, Point end, BoundingBox boundingBox) {
        final Point diff = end.sub(start);

        if (getter.getBlock(end) != Block.AIR) return false;
        PhysicsResult res = CollisionUtils.handlePhysics(getter, boundingBox,
                Pos.fromPoint(start), Vec.fromPoint(diff), null, false);
        return !res.collisionZ() && !res.collisionY() && !res.collisionX();
    }

    /**
     * Check if the point is invalid
     *
     * @param getter
     * @param point
     * @param boundingBox
     * @return true if the point is invalid
     */
    default boolean pointInvalid(Block.Getter getter, Point point, BoundingBox boundingBox) {
        var iterator = boundingBox.getBlocks(point);
        while (iterator.hasNext()) {
            var block = iterator.next();
            if (getter.getBlock(block.blockX(), block.blockY(), block.blockZ(), Block.Getter.Condition.TYPE).isSolid()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Heuristic use for the distance from the node to the target
     *
     * @param node
     * @param target
     * @return the heuristic
     */
    default double heuristic(Point node, Point target) {
        return node.distance(target);
    }
}
