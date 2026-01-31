package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.PathTypeResolver;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;

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
     * @param getter      the block getter
     * @param start       the start point
     * @param end         the end point
     * @param boundingBox the bounding box
     * @return true if we can move directly from start to end
     */
    default boolean canMoveTowards(Block.Getter getter, Point start, Point end, BoundingBox boundingBox) {
        final Point diff = end.sub(start);
        PhysicsResult res = CollisionUtils.handlePhysics(getter, boundingBox, start.asPos(), diff.asVec(), null, false);
        return !res.collisionZ() && !res.collisionY() && !res.collisionX();
    }

    /**
     * Check if the point is invalid
     *
     * @param getter      the block getter
     * @param point       the point to check
     * @param boundingBox the bounding box
     * @return true if the point is invalid
     */
    default boolean pointInvalid(Block.Getter getter, Point point, BoundingBox boundingBox) {
        var iterator = boundingBox.getBlocks(point);
        while (iterator.hasNext()) {
            var block = iterator.next();
            var blockType = getter.getBlock(block.blockX(), block.blockY(), block.blockZ(), Block.Getter.Condition.TYPE);
            if (blockType.registry().collisionShape().intersectBox(point.sub(new Vec(block.x(), block.y(), block.z())), boundingBox)) {
                return true;
            }
        }
        return false;
    }

    default PathType resolvePathType(Block.Getter getter, Point point) {
        return PathTypeResolver.getPathType(getter, point);
    }

    default void setPathMalusProvider(Function<PathType, Float> provider) {}

    default float pathMalus(PathType type) {
        return type.getMalus();
    }

    default void setMaxUpStep(double stepHeight) {}

    default double maxUpStep() {
        return 1.0;
    }

    default void setCanFloat(boolean canFloat) {}

    default boolean canFloatFlag() {
        return false;
    }

    default void setMaxFallDistance(int maxFallDistance) {}

    default int maxFallDistance() {
        return 5;
    }

    /**
     * Heuristic use for the distance from the node to the target
     *
     * @param node   the current node
     * @param target the target point
     * @return the heuristic
     */
    default double heuristic(Point node, Point target) {
        return node.distance(target);
    }
}
