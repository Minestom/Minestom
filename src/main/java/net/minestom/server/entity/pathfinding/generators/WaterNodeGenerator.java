package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class WaterNodeGenerator implements NodeGenerator {
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

                if (instance.getBlock(currentLevelPoint).compare(Block.WATER)) {
                    var nodeWalk = createFly(instance, currentLevelPoint, boundingBox, cost, current, goal, visited);
                    if (nodeWalk != null && !visited.contains(nodeWalk)) nearby.add(nodeWalk);
                }

                if (instance.getBlock(upPoint).compare(Block.WATER)) {
                    var nodeJump = createFly(instance, upPoint, boundingBox, cost, current, goal, visited);
                    if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);
                }

                if (instance.getBlock(downPoint).compare(Block.WATER)) {
                    var nodeFall = createFly(instance, downPoint, boundingBox, cost, current, goal, visited);
                    if (nodeFall != null && !visited.contains(nodeFall)) nearby.add(nodeFall);
                }
            }
        }

        // Straight up
        Point upPoint = current.point().withY(current.point().blockY() + 1 + 0.5);
        if (instance.getBlock(upPoint).compare(Block.WATER)) {
            var nodeJump = createFly(instance, upPoint, boundingBox, 2, current, goal, visited);
            if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);
        }

        // Straight down
        Point downPoint = current.point().withY(current.point().blockY() - 1 + 0.5);
        if (instance.getBlock(downPoint).compare(Block.WATER)) {
            var nodeFall = createFly(instance, downPoint, boundingBox, 2, current, goal, visited);
            if (nodeFall != null && !visited.contains(nodeFall)) nearby.add(nodeFall);
        }

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

    public @Nullable Point gravitySnap(@NotNull Instance instance, @NotNull Point point, @NotNull BoundingBox boundingBox, double maxFall) {
        point = new Pos(point.blockX() + 0.5, point.blockY(), point.blockZ() + 0.5);

        Chunk c = instance.getChunkAt(point);
        if (c == null) return null;

        for (int axis = 1; axis <= maxFall; ++axis) {
            var iterator = boundingBox.getBlocks(point, BoundingBox.AxisMask.Y, -axis);

            while (iterator.hasNext()) {
                var block = iterator.next();

                var foundBlock = instance.getBlock(block, Block.Getter.Condition.TYPE);
                // Stop falling when water is hit
                if (foundBlock.isSolid() || foundBlock.compare(Block.WATER)) {
                    return point.withY(block.blockY() + 1);
                }
            }
        }

        return point.withY(point.y() - maxFall);
    }
}
