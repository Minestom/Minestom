package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;

public class WaterNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;
    private final BoundingBox.PointIterator pointIterator = new BoundingBox.PointIterator();

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

                double currentLevelPointX = current.blockX() + 0.5 + x;
                double currentLevelPointY = current.blockY();
                double currentLevelPointZ = current.blockZ() + 0.5 + z;

                double upPointX = current.blockX() + 0.5 + x;
                double upPointY = current.blockY() + 1 + 0.5;
                double upPointZ = current.blockZ() + 0.5 + z;

                double downPointX = current.blockX() + 0.5 + x;
                double downPointY = current.blockY() - 1 + 0.5;
                double downPointZ = current.blockZ() + 0.5 + z;

                if (getter.getBlock((int) Math.floor(currentLevelPointX), (int) Math.floor(currentLevelPointY), (int) Math.floor(currentLevelPointZ)).compare(Block.WATER)) {
                    var nodeWalk = createFly(getter, new Vec(currentLevelPointX, currentLevelPointY, currentLevelPointZ), boundingBox, cost, current, goal, visited);
                    if (nodeWalk != null && !visited.contains(nodeWalk)) nearby.add(nodeWalk);
                }

                if (getter.getBlock((int) Math.floor(upPointX), (int) Math.floor(upPointY), (int) Math.floor(upPointZ)).compare(Block.WATER)) {
                    var nodeJump = createFly(getter, new Vec(upPointX, upPointY, upPointZ), boundingBox, cost, current, goal, visited);
                    if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);
                }

                if (getter.getBlock((int) Math.floor(downPointX), (int) Math.floor(downPointY), (int) Math.floor(downPointZ)).compare(Block.WATER)) {
                    var nodeFall = createFly(getter, new Vec(downPointX, downPointY, downPointZ), boundingBox, cost, current, goal, visited);
                    if (nodeFall != null && !visited.contains(nodeFall)) nearby.add(nodeFall);
                }
            }
        }

        // Straight up
        double upPointX = current.x();
        double upPointY = current.blockY() + 1 + 0.5;
        double upPointZ = current.z();

        if (getter.getBlock((int) Math.floor(upPointX), (int) Math.floor(upPointY), (int) Math.floor(upPointZ)).compare(Block.WATER)) {
            var nodeJump = createFly(getter, new Vec(current.x(), current.y(), current.z()), boundingBox, 2, current, goal, visited);
            if (nodeJump != null && !visited.contains(nodeJump)) nearby.add(nodeJump);
        }

        // Straight down
        double downPointX = current.x();
        double downPointY = current.blockY() - 1 + 0.5;
        double downPointZ = current.z();

        if (getter.getBlock((int) Math.floor(downPointX), (int) Math.floor(downPointY), (int) Math.floor(downPointZ)).compare(Block.WATER)) {
            var nodeFall = createFly(getter, new Vec(downPointX, downPointY, downPointZ), boundingBox, 2, current, goal, visited);
            if (nodeFall != null && !visited.contains(nodeFall)) nearby.add(nodeFall);
        }

        return nearby;
    }

    private PNode createFly(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        var n = newNode(start, cost, point, goal);
        if (closed.contains(n)) return null;
        if (!canMoveTowards(getter, new Vec(start.x(), start.y(), start.z()), point, boundingBox)) return null;
        n.setType(PNode.Type.FLY);
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
        return false;
    }

    @Override
    public @NotNull OptionalDouble gravitySnap(Block.@NotNull Getter getter, double pointX, double pointY, double pointZ, @NotNull BoundingBox boundingBox, double maxFall) {
        return OptionalDouble.of(pointY);
    }
}
