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

public class FlyingNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;

    @Override
    public boolean requiresGroundStart() {
        return false;
    }

    @Override
    public @NotNull Collection<? extends PNode> getWalkable(@NotNull Instance instance, @NotNull Set<PNode> visited, @NotNull PNode current, @NotNull Point goal, @NotNull BoundingBox boundingBox) {
        Collection<PNode> nearby = new ArrayList<>();
        tempNode = new PNode(Pos.ZERO, 0, 0, current);

        int stepSize = (int) Math.max(Math.floor(boundingBox.width() / 2), 1);
        if (stepSize < 1) stepSize = 1;

        for (int x = -stepSize; x <= stepSize; ++x) {
            for (int z = -stepSize; z <= stepSize; ++z) {
                if (x == 0 && z == 0) continue;
                double cost = Math.sqrt(x * x + z * z) * 0.98;

                Point currentLevelPoint = current.point().withX(current.point().blockX() + 0.5 + x).withZ(current.point().blockZ() + 0.5 + z).withY(current.point().blockY() + 0.5);
                Point upPoint = current.point().withX(current.point().blockX() + 0.5 + x).withZ(current.point().blockZ() + 0.5 + z).withY(current.point().blockY() + 1 + 0.5);
                Point downPoint = current.point().withX(current.point().blockX() + 0.5 + x).withZ(current.point().blockZ() + 0.5 + z).withY(current.point().blockY() - 1 + 0.5);

                var nodeWalk = createFly(instance, currentLevelPoint, boundingBox, cost, current, goal, visited);
                if (nodeWalk != null && !visited.contains(nodeWalk)) nearby.add(nodeWalk);

                var nodeJump = createFly(instance, upPoint, boundingBox, cost, current, goal, visited);
                if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);

                var nodeFall = createFly(instance, downPoint, boundingBox, cost, current, goal, visited);
                if (nodeFall != null && !visited.contains(nodeFall)) nearby.add(nodeFall);
            }
        }

        // Straight up
        Point upPoint = current.point().withY(current.point().blockY() + 1 + 0.5);
        var nodeJump = createFly(instance, upPoint, boundingBox, 2, current, goal, visited);
        if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);

        // Straight down
        Point downPoint = current.point().withY(current.point().blockY() - 1 + 0.5);
        var nodeFall = createFly(instance, downPoint, boundingBox, 2, current, goal, visited);
        if (nodeFall != null && !visited.contains(nodeFall)) nearby.add(nodeFall);

        return nearby;
    }

    private PNode createFly(Instance instance, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        var n = newNode(start, cost, point, goal);
        if (closed.contains(n)) return null;
        if (!canMoveTowards(instance, start.point(), point, boundingBox)) return null;
        n.setType(PNode.NodeType.FLY);
        return n;
    }

    private PNode newNode(PNode current, double cost, Point point, Point goal) {
        tempNode.setG(current.g() + cost);
        tempNode.setH(heuristic(point, goal));
        tempNode.setPoint(point);

        var newNode = tempNode;
        tempNode = new PNode(Pos.ZERO, 0, 0, PNode.NodeType.WALK, current);

        return newNode;
    }
}
