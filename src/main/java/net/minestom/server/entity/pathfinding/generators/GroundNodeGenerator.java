package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.entity.pathfinding.PathGenerator;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class GroundNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;

    @Override
    public boolean requiresGroundStart() {
        return true;
    }

    @Override
    public Collection<? extends PNode> getWalkable(Instance instance, Set<PNode> visited, PNode current, Point goal, @NotNull BoundingBox boundingBox) {
        Collection<PNode> nearby = new ArrayList<>();
        tempNode = new PNode(Pos.ZERO, 0, 0, current);

        int stepSize = (int) Math.max(Math.floor(boundingBox.width() / 2), 1);
        if (stepSize < 1) stepSize = 1;

        for (int x = -stepSize; x <= stepSize; ++x) {
            for (int z = -stepSize; z <= stepSize; ++z) {
                if (x == 0 && z == 0) continue;
                double cost = Math.sqrt(x * x + z * z) * 0.98;

                Point floorPoint = current.point().withX(current.point().blockX() + 0.5 + x).withZ(current.point().blockZ() + 0.5 + z);
                floorPoint = gravitySnap(instance, floorPoint, boundingBox, 5);
                if (floorPoint == null) continue;

                var nodeWalk = createWalk(instance, floorPoint, boundingBox, cost, current, goal, visited);
                if (nodeWalk != null && !visited.contains(nodeWalk)) nearby.add(nodeWalk);

                for (int i = 1; i <= 1; ++i) {
                    Point jumpPoint = current.point().withX(current.point().blockX() + 0.5 + x).withZ(current.point().blockZ() + 0.5 + z).add(0, i, 0);
                    jumpPoint = gravitySnap(instance, jumpPoint, boundingBox, 5);

                    if (jumpPoint == null) continue;
                    if (!floorPoint.sameBlock(jumpPoint)) {
                        var nodeJump = createJump(instance, jumpPoint, boundingBox, cost + 0.2, current, goal, visited);
                        if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);
                    }
                }
            }
        }

        return nearby;
    }

    private PNode createWalk(Instance instance, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        var n = newNode(start, cost, point, goal);
        if (closed.contains(n)) return null;

        if (point.y() < start.point().y()) {
            if (!canMoveTowards(instance, start.point(), point.withY(start.point().y()), boundingBox)) return null;
            n.setType(PNode.NodeType.FALL);
        } else {
            if (!canMoveTowards(instance, start.point(), point, boundingBox)) return null;
        }
        return n;
    }

    private PNode createJump(Instance instance, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        if (point.y() - start.point().y() == 0) return null;
        if (point.y() - start.point().y() > 2) return null;
        if (point.blockX() != start.point().blockX() && point.blockZ() != start.point().blockZ()) return null;

        var n = newNode(start, cost, point, goal);
        if (closed.contains(n)) return null;

        if (pointInvalid(instance, point, boundingBox)) return null;
        if (pointInvalid(instance, start.point().add(0, 1, 0), boundingBox)) return null;

        n.setType(PNode.NodeType.JUMP);
        return n;
    }

    private PNode newNode(PNode current, double cost, Point point, Point goal) {
        tempNode.setG(current.g() + cost);
        tempNode.setH(PathGenerator.heuristic(point, goal));
        tempNode.setPoint(point);

        var newNode = tempNode;
        tempNode = new PNode(Pos.ZERO, 0, 0, PNode.NodeType.WALK, current);

        return newNode;
    }
}
