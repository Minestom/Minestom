package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;

public class PreciseGroundNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;
    private final static int MAX_FALL_DISTANCE = 5;

    @Override
    public @NotNull Collection<? extends PNode> getWalkable(@NotNull Instance instance, @NotNull Set<PNode> visited, @NotNull PNode current, @NotNull Point goal, @NotNull BoundingBox boundingBox) {
        Collection<PNode> nearby = new ArrayList<>();
        tempNode = new PNode(0, 0, 0, 0, 0, current);

        int stepSize = (int) Math.max(Math.floor(boundingBox.width() / 2), 1);
        if (stepSize < 1) stepSize = 1;

        for (int x = -stepSize; x <= stepSize; ++x) {
            for (int z = -stepSize; z <= stepSize; ++z) {
                if (x == 0 && z == 0) continue;
                double cost = Math.sqrt(x * x + z * z) * 0.98;

                double floorPointX = current.blockX() + 0.5 + x;
                double floorPointY = current.y();
                double floorPointZ = current.blockZ() + 0.5 + z;

                var optionalFloorPointY = gravitySnap(instance, floorPointX, floorPointY, floorPointZ, boundingBox, MAX_FALL_DISTANCE);
                if (optionalFloorPointY.isEmpty()) continue;
                floorPointY = optionalFloorPointY.getAsDouble();

                var floorPoint = new Vec(floorPointX, floorPointY, floorPointZ);
                var nodeWalk = createWalk(instance, floorPoint, boundingBox, cost, current, goal, visited);

                if (nodeWalk != null && !visited.contains(nodeWalk)) nearby.add(nodeWalk);

                for (int i = 1; i <= 1; ++i) {
                    Point jumpPoint = new Vec(current.blockX() + 0.5 + x, current.y() + i, current.blockZ() + 0.5 + z);
                    OptionalDouble jumpPointY = gravitySnap(instance, jumpPoint.x(), jumpPoint.y(), jumpPoint.z(), boundingBox, MAX_FALL_DISTANCE);
                    if (jumpPointY.isEmpty()) continue;
                    jumpPoint = jumpPoint.withY(jumpPointY.getAsDouble());

                    if (!floorPoint.sameBlock(jumpPoint)) {
                        var nodeJump = createJump(instance, jumpPoint, boundingBox, cost + 0.8, current, goal, visited);
                        if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);
                    }
                }
            }
        }

        return nearby;
    }

    @Override
    public boolean hasGravitySnap() {
        return true;
    }

    private PNode createWalk(Instance instance, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        var snapped = gravitySnap(instance, point.x(), point.y(), point.z(), boundingBox, MAX_FALL_DISTANCE);

        if (snapped.isPresent()) {
            var snappedPoint = new Vec(point.x(), snapped.getAsDouble(), point.z());

            var n = newNode(start, cost, snappedPoint, goal);
            if (closed.contains(n)) {
                return null;
            }

            if (Math.abs(snappedPoint.y() - start.y()) > Vec.EPSILON && snappedPoint.y() < start.y()) {
                if (start.y() - snappedPoint.y() > MAX_FALL_DISTANCE) {
                    return null;
                }
                if (!canMoveTowards(instance, new Vec(start.x(), start.y(), start.z()), snappedPoint.withY(start.y()), boundingBox)) {
                    return null;
                }
                n.setType(PNode.NodeType.FALL);
            } else {
                if (!canMoveTowards(instance, new Vec(start.x(), start.y(), start.z()), snappedPoint, boundingBox)) {
                    return null;
                }
            }

            return n;
        } else {
            return null;
        }
    }

    private PNode createJump(Instance instance, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        if (Math.abs(point.y() - start.y()) < Vec.EPSILON) return null;
        if (point.y() - start.y() > 2) return null;
        if (point.blockX() != start.blockX() && point.blockZ() != start.blockZ()) return null;

        var n = newNode(start, cost, point, goal);
        if (closed.contains(n)) return null;

        if (pointInvalid(instance, point, boundingBox)) return null;
        if (pointInvalid(instance, new Vec(start.x(), start.y() + 1, start.z()), boundingBox)) return null;

        n.setType(PNode.NodeType.JUMP);
        return n;
    }

    private PNode newNode(PNode current, double cost, Point point, Point goal) {
        tempNode.setG(current.g() + cost);
        tempNode.setH(heuristic(point, goal));
        tempNode.setPoint(point.x(), point.y(), point.z());

        var newNode = tempNode;
        tempNode = new PNode(0, 0, 0, 0, 0, PNode.NodeType.WALK, current);

        return newNode;
    }

    @Override
    public @NotNull OptionalDouble gravitySnap(@NotNull Instance instance, double pointOrgX, double pointOrgY, double pointOrgZ, @NotNull BoundingBox boundingBox, double maxFall) {
        double pointX = (int) Math.floor(pointOrgX) + 0.5;
        double pointZ = (int) Math.floor(pointOrgZ) + 0.5;
        var res= CollisionUtils.handlePhysics(instance, boundingBox, new Pos(pointX, pointOrgY, pointZ), new Vec(0, -MAX_FALL_DISTANCE, 0), null, true);
        return OptionalDouble.of(res.newPosition().y());
    }

    @Override
    public boolean canMoveTowards(@NotNull Instance instance, @NotNull Point startOrg, @NotNull Point endOrg, @NotNull BoundingBox boundingBox) {
        var end = endOrg.add(0, Vec.EPSILON, 0);
        var start = startOrg.add(0, Vec.EPSILON, 0);

        Point diff = end.sub(start);
        PhysicsResult res = CollisionUtils.handlePhysics(instance, instance.getChunkAt(start), boundingBox, Pos.fromPoint(start), Vec.fromPoint(diff), null, false);
        return !res.collisionZ() && !res.collisionY() && !res.collisionX();
    }
}
