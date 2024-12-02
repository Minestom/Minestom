package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;

public class PreciseGroundNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;
    private final static int MAX_FALL_DISTANCE = 5;

    @Override
    public @NotNull Collection<? extends PNode> getWalkable(Block.@NotNull Getter getter, @NotNull Set<PNode> visited, @NotNull PNode current, @NotNull Point goal, @NotNull BoundingBox boundingBox) {
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

                var optionalFloorPointY = gravitySnap(getter, floorPointX, floorPointY, floorPointZ, boundingBox, MAX_FALL_DISTANCE);
                if (optionalFloorPointY.isEmpty()) continue;
                floorPointY = optionalFloorPointY.getAsDouble();

                var floorPoint = new Vec(floorPointX, floorPointY, floorPointZ);
                var nodeWalk = createWalk(getter, floorPoint, boundingBox, cost, current, goal, visited);

                if (nodeWalk != null && !visited.contains(nodeWalk)) nearby.add(nodeWalk);

                for (int i = 1; i <= 1; ++i) {
                    Point jumpPoint = new Vec(current.blockX() + 0.5 + x, current.y() + i, current.blockZ() + 0.5 + z);
                    OptionalDouble jumpPointY = gravitySnap(getter, jumpPoint.x(), jumpPoint.y(), jumpPoint.z(), boundingBox, MAX_FALL_DISTANCE);
                    if (jumpPointY.isEmpty()) continue;
                    jumpPoint = jumpPoint.withY(jumpPointY.getAsDouble());

                    if (!floorPoint.sameBlock(jumpPoint)) {
                        var nodeJump = createJump(getter, jumpPoint, boundingBox, cost + 0.8, current, goal, visited);
                        if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);
                    }
                }
            }
        }

        return nearby;
    }

    private PNode createWalk(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        var snapped = gravitySnap(getter, point.x(), point.y(), point.z(), boundingBox, MAX_FALL_DISTANCE);

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
                if (!canMoveTowards(getter, new Vec(start.x(), start.y(), start.z()), snappedPoint.withY(start.y()), boundingBox)) {
                    return null;
                }
                n.setType(PNode.Type.FALL);
            } else {
                if (!canMoveTowards(getter, new Vec(start.x(), start.y(), start.z()), snappedPoint, boundingBox)) {
                    return null;
                }
            }

            return n;
        } else {
            return null;
        }
    }

    private PNode createJump(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        if (Math.abs(point.y() - start.y()) < Vec.EPSILON) return null;
        if (point.y() - start.y() > 2) return null;
        if (point.blockX() != start.blockX() && point.blockZ() != start.blockZ()) return null;

        var n = newNode(start, cost, point, goal);
        if (closed.contains(n)) return null;

        if (pointInvalid(getter, point, boundingBox)) return null;
        if (pointInvalid(getter, new Vec(start.x(), start.y() + 1, start.z()), boundingBox)) return null;

        n.setType(PNode.Type.JUMP);
        return n;
    }

    private PNode newNode(PNode current, double cost, Point point, Point goal) {
        tempNode.setG(current.g() + cost);
        tempNode.setH(heuristic(point, goal));
        tempNode.setPoint(point.x(), point.y(), point.z());

        var newNode = tempNode;
        tempNode = new PNode(0, 0, 0, 0, 0, PNode.Type.WALK, current);

        return newNode;
    }

    @Override
    public boolean hasGravitySnap() {
        return true;
    }

    @Override
    public @NotNull OptionalDouble gravitySnap(Block.@NotNull Getter getter, double pointOrgX, double pointOrgY, double pointOrgZ, @NotNull BoundingBox boundingBox, double maxFall) {
        final double pointX = (int) Math.floor(pointOrgX) + 0.5;
        final double pointZ = (int) Math.floor(pointOrgZ) + 0.5;
        final PhysicsResult res = CollisionUtils.handlePhysics(getter, boundingBox,
                new Pos(pointX, pointOrgY, pointZ), new Vec(0, -MAX_FALL_DISTANCE, 0),
                null, true);
        return OptionalDouble.of(res.newPosition().y());
    }

    @Override
    public boolean canMoveTowards(Block.@NotNull Getter getter, @NotNull Point startOrg, @NotNull Point endOrg, @NotNull BoundingBox boundingBox) {
        final Point end = endOrg.add(0, Vec.EPSILON, 0);
        final Point start = startOrg.add(0, Vec.EPSILON, 0);
        final Point diff = end.sub(start);
        PhysicsResult res = CollisionUtils.handlePhysics(getter, boundingBox, Pos.fromPoint(start), Vec.fromPoint(diff), null, false);
        return !res.collisionZ() && !res.collisionY() && !res.collisionX();
    }
}
