package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;

public interface NodeGenerator {
    /**
     * Gets the walkable nodes.
     *
     * @param instance    the instance
     * @param visited     the visited nodes
     * @param current     the current node
     * @param goal        the goal
     * @param boundingBox the bounding box
     * @return the walkable nodes
     */
    @NotNull Collection<? extends PNode> getWalkable(@NotNull Instance instance, @NotNull Set<PNode> visited, @NotNull PNode current, @NotNull Point goal, @NotNull BoundingBox boundingBox);

    /**
     * @return snap start and end points to the ground
     */
    boolean hasGravitySnap();

    /**
     * Snap point to the ground
     * @param instance the instance
     * @param pointX the x coordinate
     * @param pointY the y coordinate
     * @param pointZ the z coordinate
     * @param boundingBox the bounding box
     * @param maxFall the maximum fall distance
     * @return the snapped y coordinate. Empty if the snap point is not found
     */
    @NotNull OptionalDouble gravitySnap(@NotNull Instance instance, double pointX, double pointY, double pointZ, @NotNull BoundingBox boundingBox, double maxFall);

    /**
     * Check if we can move directly from one point to another
     * @param instance
     * @param start
     * @param end
     * @param boundingBox
     * @return true if we can move directly from start to end
     */
    default boolean canMoveTowards(@NotNull Instance instance, @NotNull Point start, @NotNull Point end, @NotNull BoundingBox boundingBox) {
        Point diff = end.sub(start);

        if (instance.getBlock(end) != Block.AIR) return false;
        PhysicsResult res = CollisionUtils.handlePhysics(instance, instance.getChunkAt(start), boundingBox, Pos.fromPoint(start), Vec.fromPoint(diff), null, false);
        return !res.collisionZ() && !res.collisionY() && !res.collisionX();
    }

    /**
     * Check if the point is invalid
     * @param instance
     * @param point
     * @param boundingBox
     * @return true if the point is invalid
     */
    default boolean pointInvalid(@NotNull Instance instance, @NotNull Point point, @NotNull BoundingBox boundingBox) {
        var iterator = boundingBox.getBlocks(point);
        while (iterator.hasNext()) {
            var block = iterator.next();
            if (instance.getBlock(block.blockX(), block.blockY(), block.blockZ(), Block.Getter.Condition.TYPE).isSolid()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Heuristic use for the distance from the node to the target
     * @param node
     * @param target
     * @return the heuristic
     */
    default double heuristic(@NotNull Point node, @NotNull Point target) {
        return node.distance(target);
    }
}
