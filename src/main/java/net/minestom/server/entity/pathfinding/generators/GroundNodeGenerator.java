package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;

public class GroundNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;
    private final BoundingBox.PointIterator pointIterator = new BoundingBox.PointIterator();
    private static final int MAX_FALL_DISTANCE = 5;
    private Function<PathType, Float> malusProvider = PathType::getMalus;
    private double maxStepHeight = 1.0;
    private boolean canFloat;

    @Override
    public Collection<? extends PNode> getWalkable(Block.Getter getter, Set<PNode> visited, PNode current, Point goal, BoundingBox boundingBox) {
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

                var optionalFloorPointY = gravitySnap(getter, floorPointX, floorPointY, floorPointZ, boundingBox, MAX_FALL_DISTANCE);
                if (optionalFloorPointY.isEmpty()) continue;
                floorPointY = optionalFloorPointY.getAsDouble();

                var floorPoint = new Vec(floorPointX, floorPointY, floorPointZ);

                var nodeWalk = createWalk(getter, floorPoint, boundingBox, cost, current, goal, visited);
                if (nodeWalk != null && !visited.contains(nodeWalk)) nearby.add(nodeWalk);

                final int maxJumpHeight = (int) Math.ceil(Math.max(1.0, maxStepHeight));
                for (int i = 1; i <= maxJumpHeight; ++i) {
                    Point jumpPoint = new Vec(current.blockX() + 0.5 + x, current.blockY() + i, current.blockZ() + 0.5 + z);
                    OptionalDouble jumpPointY = gravitySnap(getter, jumpPoint.x(), jumpPoint.y(), jumpPoint.z(), boundingBox, MAX_FALL_DISTANCE);
                    if (jumpPointY.isEmpty()) continue;
                    jumpPoint = jumpPoint.withY(jumpPointY.getAsDouble());

                    if (!floorPoint.sameBlock(jumpPoint)) {
                        var nodeJump = createJump(getter, jumpPoint, boundingBox, cost + 0.2, current, goal, visited);
                        if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);
                    }
                }
            }
        }

        return nearby;
    }

    private PNode createWalk(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        var n = newNode(getter, start, cost, point, goal);
        if (n == null || closed.contains(n)) return null;

        if (point.y() - start.y() > maxStepHeight + Vec.EPSILON) return null;

        if (Math.abs(point.y() - start.y()) > Vec.EPSILON && point.y() < start.y()) {
            if (start.y() - point.y() > MAX_FALL_DISTANCE) return null;
            if (!canMoveTowards(getter, new Vec(start.x(), start.y(), start.z()), point.withY(start.y()), boundingBox))
                return null;
            n.setType(PNode.Type.FALL);
        } else {
            if (!canMoveTowards(getter, new Vec(start.x(), start.y(), start.z()), point, boundingBox)) return null;
        }
        return n;
    }

    private PNode createJump(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        if (Math.abs(point.y() - start.y()) < Vec.EPSILON) return null;
        if (point.y() - start.y() > maxStepHeight + 0.5) return null;
        if (point.blockX() != start.blockX() && point.blockZ() != start.blockZ()) return null;

        var n = newNode(getter, start, cost, point, goal);
        if (n == null || closed.contains(n)) return null;

        if (pointInvalid(getter, point, boundingBox)) return null;
        if (pointInvalid(getter, new Vec(start.x(), start.y() + 1, start.z()), boundingBox)) return null;

        n.setType(PNode.Type.JUMP);
        return n;
    }

    private PNode newNode(Block.Getter getter, PNode current, double cost, Point point, Point goal) {
        PathType pathType = resolvePathType(getter, point);
        if (pathType.getMalus() < 0) return null;

        tempNode.setG(current.g() + cost + malusProvider.apply(pathType));
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
    public OptionalDouble gravitySnap(Block.Getter getter, double pointOrgX, double pointOrgY, double pointOrgZ, BoundingBox boundingBox, double maxFall) {
        final double pointX = (int) Math.floor(pointOrgX) + 0.5;
        final double pointY = (int) Math.floor(pointOrgY);
        final double pointZ = (int) Math.floor(pointOrgZ) + 0.5;

        for (int axis = 1; axis <= maxFall; ++axis) {
            pointIterator.reset(boundingBox, pointX, pointY, pointZ, BoundingBox.AxisMask.Y, -axis);

            while (pointIterator.hasNext()) {
                var block = pointIterator.next();
                Block b = getter.getBlock(block.blockX(), block.blockY(), block.blockZ(), Block.Getter.Condition.TYPE);
                if (b.isSolid() || !b.registry().collisionShape().relativeEnd().isZero()) {
                    return OptionalDouble.of(block.blockY() + 1);
                }
            }
        }
        return OptionalDouble.empty();
    }

    @Override
    public void setPathMalusProvider(Function<PathType, Float> provider) {
        this.malusProvider = provider;
    }

    @Override
    public float pathMalus(PathType type) {
        return malusProvider.apply(type);
    }

    @Override
    public void setMaxUpStep(double stepHeight) {
        this.maxStepHeight = Math.max(0.0, stepHeight);
    }

    @Override
    public double maxUpStep() {
        return maxStepHeight;
    }

    @Override
    public void setCanFloat(boolean canFloat) {
        this.canFloat = canFloat;
    }

    @Override
    public boolean canFloatFlag() {
        return canFloat;
    }
}
