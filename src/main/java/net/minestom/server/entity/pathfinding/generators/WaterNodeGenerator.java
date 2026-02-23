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

public class WaterNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;
    private final BoundingBox.PointIterator pointIterator = new BoundingBox.PointIterator();
    private Function<PathType, Float> malusProvider = PathType::getMalus;
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

                double levelX = current.blockX() + 0.5 + x;
                double levelY = current.blockY();
                double levelZ = current.blockZ() + 0.5 + z;

                double upX = current.blockX() + 0.5 + x;
                double upY = current.blockY() + 1 + 0.5;
                double upZ = current.blockZ() + 0.5 + z;

                double downX = current.blockX() + 0.5 + x;
                double downY = current.blockY() - 1 + 0.5;
                double downZ = current.blockZ() + 0.5 + z;

                if (getter.getBlock((int) Math.floor(levelX), (int) Math.floor(levelY), (int) Math.floor(levelZ)).compare(Block.WATER)) {
                    var node = createSwim(getter, new Vec(levelX, levelY, levelZ), boundingBox, cost, current, goal, visited);
                    if (node != null && !visited.contains(node)) nearby.add(node);
                }

                if (getter.getBlock((int) Math.floor(upX), (int) Math.floor(upY), (int) Math.floor(upZ)).compare(Block.WATER)) {
                    var node = createSwim(getter, new Vec(upX, upY, upZ), boundingBox, cost, current, goal, visited);
                    if (node != null && !visited.contains(node)) nearby.add(node);
                }

                if (getter.getBlock((int) Math.floor(downX), (int) Math.floor(downY), (int) Math.floor(downZ)).compare(Block.WATER)) {
                    var node = createSwim(getter, new Vec(downX, downY, downZ), boundingBox, cost, current, goal, visited);
                    if (node != null && !visited.contains(node)) nearby.add(node);
                }
            }
        }

        double upX = current.x();
        double upY = current.blockY() + 1 + 0.5;
        double upZ = current.z();

        if (getter.getBlock((int) Math.floor(upX), (int) Math.floor(upY), (int) Math.floor(upZ)).compare(Block.WATER)) {
            var node = createSwim(getter, new Vec(current.x(), current.y(), current.z()), boundingBox, 2, current, goal, visited);
            if (node != null && !visited.contains(node)) nearby.add(node);
        }

        double downX = current.x();
        double downY = current.blockY() - 1 + 0.5;
        double downZ = current.z();

        if (getter.getBlock((int) Math.floor(downX), (int) Math.floor(downY), (int) Math.floor(downZ)).compare(Block.WATER)) {
            var node = createSwim(getter, new Vec(downX, downY, downZ), boundingBox, 2, current, goal, visited);
            if (node != null && !visited.contains(node)) nearby.add(node);
        }

        return nearby;
    }

    private PNode createSwim(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
        var n = newNode(getter, start, cost, point, goal);
        if (n == null || closed.contains(n)) return null;
        if (!canMoveTowards(getter, new Vec(start.x(), start.y(), start.z()), point, boundingBox)) return null;
        n.setType(PNode.Type.FLY);
        return n;
    }

    private PNode newNode(Block.Getter getter, PNode current, double cost, Point point, Point goal) {
        PathType pathType = resolvePathType(getter, point);
        float malus = malusProvider.apply(pathType);
        if (malus < 0) return null;

        tempNode.setG(current.g() + cost + malus);
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
    public OptionalDouble gravitySnap(Block.Getter getter, double pointX, double pointY, double pointZ, BoundingBox boundingBox, double maxFall) {
        return OptionalDouble.of(pointY);
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
    public void setCanFloat(boolean canFloat) {
        this.canFloat = canFloat;
    }

    @Override
    public boolean canFloatFlag() {
        return canFloat;
    }
}
