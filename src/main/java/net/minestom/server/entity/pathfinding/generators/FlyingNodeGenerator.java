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

public class FlyingNodeGenerator implements NodeGenerator {
    private PNode tempNode = null;
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

                double levelX = current.blockX() + 0.5 + x;
                double levelY = current.blockY() + 0.5;
                double levelZ = current.blockZ() + 0.5 + z;

                double upX = current.blockX() + 0.5 + x;
                double upY = current.blockY() + 1 + 0.5;
                double upZ = current.blockZ() + 0.5 + z;

                double downX = current.blockX() + 0.5 + x;
                double downY = current.blockY() - 1 + 0.5;
                double downZ = current.blockZ() + 0.5 + z;

                var nodeLevel = createFly(getter, new Vec(levelX, levelY, levelZ), boundingBox, cost, current, goal, visited);
                if (nodeLevel != null && !visited.contains(nodeLevel)) nearby.add(nodeLevel);

                var nodeUp = createFly(getter, new Vec(upX, upY, upZ), boundingBox, cost, current, goal, visited);
                if (nodeUp != null && !visited.contains(nodeUp)) nearby.add(nodeUp);

                var nodeDown = createFly(getter, new Vec(downX, downY, downZ), boundingBox, cost, current, goal, visited);
                if (nodeDown != null && !visited.contains(nodeDown)) nearby.add(nodeDown);
            }
        }

        double upX = current.x();
        double upY = current.blockY() + 1 + 0.5;
        double upZ = current.z();

        var nodeUp = createFly(getter, new Vec(upX, upY, upZ), boundingBox, 2, current, goal, visited);
        if (nodeUp != null && !visited.contains(nodeUp)) nearby.add(nodeUp);

        double downX = current.x();
        double downY = current.blockY() - 1 + 0.5;
        double downZ = current.z();

        var nodeDown = createFly(getter, new Vec(downX, downY, downZ), boundingBox, 2, current, goal, visited);
        if (nodeDown != null && !visited.contains(nodeDown)) nearby.add(nodeDown);

        return nearby;
    }

    @Override
    public boolean hasGravitySnap() {
        return false;
    }

    private PNode createFly(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode start, Point goal, Set<PNode> closed) {
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
