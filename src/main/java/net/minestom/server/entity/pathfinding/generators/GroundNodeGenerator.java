package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;

public class GroundNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;
    private final BoundingBox.PointIterator pointIterator = new BoundingBox.PointIterator();
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
                double floorPointY = current.blockY();
                double floorPointZ = current.blockZ() + 0.5 + z;

                var optionalFloorPointY = gravitySnap(instance, floorPointX, floorPointY, floorPointZ, boundingBox, MAX_FALL_DISTANCE);
                if (optionalFloorPointY.isEmpty()) continue;
                floorPointY = optionalFloorPointY.getAsDouble();

                var floorPoint = new Vec(floorPointX, floorPointY, floorPointZ);

                var nodeWalk = createWalk(instance, floorPoint, boundingBox, cost, current, goal, visited);
                if (nodeWalk != null && !visited.contains(nodeWalk)) nearby.add(nodeWalk);

                for (int i = 1; i <= 1; ++i) {
                    Point jumpPoint = new Vec(current.blockX() + 0.5 + x, current.blockY() + i, current.blockZ() + 0.5 + z);
                    OptionalDouble jumpPointY = gravitySnap(instance, jumpPoint.x(), jumpPoint.y(), jumpPoint.z(), boundingBox, MAX_FALL_DISTANCE);
                    if (jumpPointY.isEmpty()) continue;
                    jumpPoint = jumpPoint.withY(jumpPointY.getAsDouble());

                    if (!floorPoint.sameBlock(jumpPoint)) {
                        var nodeJump = createJump(instance, jumpPoint, boundingBox, cost + 0.2, current, goal, visited);
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
        var n = newNode(start, cost, point, goal);
        if (closed.contains(n)) return null;

        if (Math.abs(point.y() - start.y()) > Vec.EPSILON && point.y() < start.y()) {
            if (start.y() - point.y() > MAX_FALL_DISTANCE) return null;
            if (!canMoveTowards(instance, new Vec(start.x(), start.y(), start.z()), point.withY(start.y()), boundingBox)) return null;
            n.setType(PNode.NodeType.FALL);
        } else {
            if (!canMoveTowards(instance, new Vec(start.x(), start.y(), start.z()), point, boundingBox)) return null;
        }
        return n;
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
        double pointY = (int) Math.floor(pointOrgY);
        double pointZ = (int) Math.floor(pointOrgZ) + 0.5;

        Chunk c = instance.getChunkAt(pointX, pointZ);
        if (c == null) return OptionalDouble.of(pointY);

        for (int axis = 1; axis <= maxFall; ++axis) {
            pointIterator.reset(boundingBox, pointX, pointY, pointZ, BoundingBox.AxisMask.Y, -axis);

            while (pointIterator.hasNext()) {
                var block = pointIterator.next();

                if (instance.getBlock(block.blockX(), block.blockY(), block.blockZ(), Block.Getter.Condition.TYPE).isSolid()) {
                    return OptionalDouble.of(block.blockY() + 1);
                }
            }
        }

        return OptionalDouble.empty();
    }
}
