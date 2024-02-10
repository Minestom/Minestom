package net.minestom.server.entity.pathfinding;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class PNode {
    public enum NodeType {
        WALK,
        JUMP,
        FALL,
        CLIMB,
        CLIMB_WALL,
        SWIM,
        FLY, REPATH
    }

    double g;
    double h;
    PNode parent;
    Pos point;
    int hashCode;

    int cantor(int a, int b) {
        return (a + b + 1) * (a + b) / 2 + b;
    }

    private PNode tempNode = null;

    private NodeType type;

    void setType(NodeType newType) {
        this.type = newType;
    }

    public NodeType getType() {
        return type;
    }

    private void setPoint(Pos point) {
        this.point = point;
        this.hashCode = cantor(point.blockX(), cantor(point.blockY(), point.blockZ()));
    }

    public PNode(Pos point, double g, double h, PNode parent) {
        this(point, g, h, NodeType.WALK, parent);
    }

    public PNode(Pos point, double g, double h, NodeType type, PNode parent) {
        this.point = new Pos(point.x(), point.y(), point.z());
        this.g = g;
        this.h = h;
        this.parent = parent;
        this.hashCode = cantor(point.blockX(), cantor(point.blockY(), point.blockZ()));
        this.type = type;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof PNode other)) return false;
        return this.hashCode == other.hashCode;
    }

    public Collection<? extends PNode> getNearby(Instance instance, Set<PNode> closed, Point goal, @NotNull BoundingBox boundingBox, PPath.PathfinderCapabilities capabilities) {
        if (capabilities.type() == PPath.PathfinderType.FLYING) return getNearbyAir(instance, closed, goal, boundingBox, capabilities);
        else if (capabilities.type() == PPath.PathfinderType.AQUATIC) return getNearbyWater(instance, closed, goal, boundingBox, capabilities);
        else if (capabilities.type() == PPath.PathfinderType.AMPHIBIOUS) return getNearbyAmphibious(instance, closed, goal, boundingBox, capabilities);
        return getNearbyGround(instance, closed, goal, boundingBox, capabilities);
    }

    public Collection<? extends PNode> getNearbyWater(Instance instance, Set<PNode> closed, Point goal, @NotNull BoundingBox boundingBox, PPath.PathfinderCapabilities capabilities) {
        Collection<PNode> nearby = new ArrayList<>();
        tempNode = new PNode(Pos.ZERO, 0, 0, this);

        int stepSize = (int) Math.max(Math.floor(boundingBox.width() / 2), 1);
        if (stepSize < 1) stepSize = 1;

        for (int x = -stepSize; x <= stepSize; ++x) {
            for (int z = -stepSize; z <= stepSize; ++z) {
                if (x == 0 && z == 0) continue;
                double cost = Math.sqrt(x * x + z * z) * 0.98;

                Pos currentLevelPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() + 0.5);
                Pos upPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() + 1 + 0.5);
                Pos downPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() - 1 + 0.5);

                if (instance.getBlock(currentLevelPoint).compare(Block.WATER)) {
                    var nodeWalk = createFly(instance, currentLevelPoint, boundingBox, cost, point, goal, closed);
                    if (nodeWalk != null && !closed.contains(nodeWalk)) nearby.add(nodeWalk);
                }

                if (instance.getBlock(upPoint).compare(Block.WATER)) {
                    var nodeJump = createFly(instance, upPoint, boundingBox, cost, point, goal, closed);
                    if (nodeJump != null && !closed.contains(nodeJump)) nearby.add(nodeJump);
                }

                if (instance.getBlock(downPoint).compare(Block.WATER)) {
                    var nodeFall = createFly(instance, downPoint, boundingBox, cost, point, goal, closed);
                    if (nodeFall != null && !closed.contains(nodeFall)) nearby.add(nodeFall);
                }
            }
        }

        // Straight up
        Pos upPoint = point.withY(point.blockY() + 1 + 0.5);
        if (instance.getBlock(upPoint).compare(Block.WATER)) {
            var nodeJump = createFly(instance, upPoint, boundingBox, 2, point, goal, closed);
            if (nodeJump != null && !closed.contains(nodeJump)) nearby.add(nodeJump);
        }

        // Straight down
        Pos downPoint = point.withY(point.blockY() - 1 + 0.5);
        if (instance.getBlock(downPoint).compare(Block.WATER)) {
            var nodeFall = createFly(instance, downPoint, boundingBox, 2, point, goal, closed);
            if (nodeFall != null && !closed.contains(nodeFall)) nearby.add(nodeFall);
        }

        return nearby;
    }

    public Collection<? extends PNode> getNearbyAmphibious(Instance instance, Set<PNode> closed, Point goal, @NotNull BoundingBox boundingBox, PPath.PathfinderCapabilities capabilities) {
        Collection<PNode> nearby = new ArrayList<>();
        tempNode = new PNode(Pos.ZERO, 0, 0, this);

        int stepSize = (int) Math.max(Math.floor(boundingBox.width() / 2), 1);
        if (stepSize < 1) stepSize = 1;

        for (int x = -stepSize; x <= stepSize; ++x) {
            for (int z = -stepSize; z <= stepSize; ++z) {
                if (x == 0 && z == 0) continue;
                double cost = Math.sqrt(x * x + z * z) * 0.98;

                // Land
                {
                    Pos floorPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z);
                    floorPoint = gravitySnap(instance, floorPoint, boundingBox, 5);
                    if (floorPoint == null) continue;

                    if (!instance.getBlock(floorPoint).compare(Block.WATER)) {
                        var nodeWalk = createWalk(instance, floorPoint, boundingBox, cost, point, goal, closed);
                        if (nodeWalk != null && !closed.contains(nodeWalk)) nearby.add(nodeWalk);
                    }

                    for (int i = 1; i <= 1; ++i) {
                        Pos jumpPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).add(0, i, 0);
                        jumpPoint = gravitySnap(instance, jumpPoint, boundingBox, 5);

                        if (jumpPoint == null) continue;
                        if (!floorPoint.sameBlock(jumpPoint) && !instance.getBlock(jumpPoint).compare(Block.WATER)) {
                            var nodeJump = createJump(instance, jumpPoint, boundingBox, cost + 0.2, point, goal, closed);
                            if (nodeJump != null && !closed.contains(nodeJump)) nearby.add(nodeJump);
                        }
                    }
                }

                // Water
                {
                    Pos currentLevelPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() + 0.5);
                    Pos upPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() + 1 + 0.5);
                    Pos downPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() - 1 + 0.5);

                    if (instance.getBlock(currentLevelPoint).compare(Block.WATER)) {
                        var nodeWalk = createFly(instance, currentLevelPoint, boundingBox, cost, point, goal, closed);
                        if (nodeWalk != null && !closed.contains(nodeWalk)) nearby.add(nodeWalk);
                    }

                    if (instance.getBlock(upPoint).compare(Block.WATER)) {
                        var nodeJump = createFly(instance, upPoint, boundingBox, cost, point, goal, closed);
                        if (nodeJump != null && !closed.contains(nodeJump)) nearby.add(nodeJump);
                    }

                    if (instance.getBlock(downPoint).compare(Block.WATER)) {
                        var nodeFall = createFly(instance, downPoint, boundingBox, cost, point, goal, closed);
                        if (nodeFall != null && !closed.contains(nodeFall)) nearby.add(nodeFall);
                    }
                }
            }
        }

        // Straight up
        Pos upPoint = point.withY(point.blockY() + 1 + 0.5);
        if (instance.getBlock(upPoint).compare(Block.WATER)) {
            var nodeJump = createFly(instance, upPoint, boundingBox, 2, point, goal, closed);
            if (nodeJump != null && !closed.contains(nodeJump)) nearby.add(nodeJump);
        }

        // Straight down
        Pos downPoint = point.withY(point.blockY() - 1 + 0.5);
        if (instance.getBlock(downPoint).compare(Block.WATER)) {
            var nodeFall = createFly(instance, downPoint, boundingBox, 2, point, goal, closed);
            if (nodeFall != null && !closed.contains(nodeFall)) nearby.add(nodeFall);
        }

        return nearby;
    }

    public Collection<? extends PNode> getNearbyAir(Instance instance, Set<PNode> closed, Point goal, @NotNull BoundingBox boundingBox, PPath.PathfinderCapabilities capabilities) {
        Collection<PNode> nearby = new ArrayList<>();
        tempNode = new PNode(Pos.ZERO, 0, 0, this);

        int stepSize = (int) Math.max(Math.floor(boundingBox.width() / 2), 1);
        if (stepSize < 1) stepSize = 1;

        for (int x = -stepSize; x <= stepSize; ++x) {
            for (int z = -stepSize; z <= stepSize; ++z) {
                if (x == 0 && z == 0) continue;
                double cost = Math.sqrt(x * x + z * z) * 0.98;

                Pos currentLevelPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() + 0.5);
                Pos upPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() + 1 + 0.5);
                Pos downPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).withY(point.blockY() - 1 + 0.5);

                var nodeWalk = createFly(instance, currentLevelPoint, boundingBox, cost, point, goal, closed);
                if (nodeWalk != null && !closed.contains(nodeWalk)) nearby.add(nodeWalk);

                var nodeJump = createFly(instance, upPoint, boundingBox, cost, point, goal, closed);
                if (nodeJump != null && !closed.contains(nodeJump)) nearby.add(nodeJump);

                var nodeFall = createFly(instance, downPoint, boundingBox, cost, point, goal, closed);
                if (nodeFall != null && !closed.contains(nodeFall)) nearby.add(nodeFall);
            }
        }

        // Straight up
        Pos upPoint = point.withY(point.blockY() + 1 + 0.5);
        var nodeJump = createFly(instance, upPoint, boundingBox, 2, point, goal, closed);
        if (nodeJump != null && !closed.contains(nodeJump)) nearby.add(nodeJump);

        // Straight down
        Pos downPoint = point.withY(point.blockY() - 1 + 0.5);
        var nodeFall = createFly(instance, downPoint, boundingBox, 2, point, goal, closed);
        if (nodeFall != null && !closed.contains(nodeFall)) nearby.add(nodeFall);

        return nearby;
    }

    public Collection<? extends PNode> getNearbyGround(Instance instance, Set<PNode> closed, Point goal, @NotNull BoundingBox boundingBox, PPath.PathfinderCapabilities capabilities) {
        Collection<PNode> nearby = new ArrayList<>();
        tempNode = new PNode(Pos.ZERO, 0, 0, this);

        int stepSize = (int) Math.max(Math.floor(boundingBox.width() / 2), 1);
        if (stepSize < 1) stepSize = 1;

        for (int x = -stepSize; x <= stepSize; ++x) {
            for (int z = -stepSize; z <= stepSize; ++z) {
                if (x == 0 && z == 0) continue;
                double cost = Math.sqrt(x * x + z * z) * 0.98;

                Pos floorPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z);
                floorPoint = gravitySnap(instance, floorPoint, boundingBox, 5);
                if (floorPoint == null) continue;

                var nodeWalk = createWalk(instance, floorPoint, boundingBox, cost, point, goal, closed);
                if (nodeWalk != null && !closed.contains(nodeWalk)) nearby.add(nodeWalk);

                for (int i = 1; i <= 1; ++i) {
                    Pos jumpPoint = point.withX(point.blockX() + 0.5 + x).withZ(point.blockZ() + 0.5 + z).add(0, i, 0);
                    jumpPoint = gravitySnap(instance, jumpPoint, boundingBox, 5);

                    if (jumpPoint == null) continue;
                    if (!floorPoint.sameBlock(jumpPoint)) {
                        var nodeJump = createJump(instance, jumpPoint, boundingBox, cost + 0.2, point, goal, closed);
                        if (nodeJump != null && !closed.contains(nodeJump)) nearby.add(nodeJump);
                    }
                }
            }
        }

        return nearby;
    }

    private PNode createFly(Instance instance, Pos point, BoundingBox boundingBox, double cost, Pos start, Point goal, Set<PNode> closed) {
        var n = newNode(cost, point, goal);
        if (closed.contains(n)) return null;
        if (!canMoveTowards(instance, start, point, boundingBox)) return null;
        n.setType(NodeType.FLY);
        return n;
    }

    private PNode createWalk(Instance instance, Pos point, BoundingBox boundingBox, double cost, Pos start, Point goal, Set<PNode> closed) {
        var n = newNode(cost, point, goal);
        if (closed.contains(n)) return null;

        if (point.y() < start.y()) {
            if (!canMoveTowards(instance, start, point.withY(start.y()), boundingBox)) return null;
            n.setType(NodeType.FALL);
        } else {
            if (!canMoveTowards(instance, start, point, boundingBox)) return null;
        }
        return n;
    }

    private PNode createJump(Instance instance, Pos point, BoundingBox boundingBox, double cost, Pos start, Point goal, Set<PNode> closed) {
        if (point.y() - start.y() == 0) return null;
        if (point.y() - start.y() > 2) return null;
        if (point.blockX() != start.blockX() && point.blockZ() != start.blockZ()) return null;

        var n = newNode(cost, point, goal);
        if (closed.contains(n)) return null;

        if (pointInvalid(instance, point, boundingBox)) return null;
        if (pointInvalid(instance, start.add(0, 1, 0), boundingBox)) return null;

        n.setType(NodeType.JUMP);
        return n;
    }

    private boolean pointInvalid(Instance instance, Pos point, BoundingBox boundingBox) {
        var iterator = boundingBox.getBlocks(point);
        while (iterator.hasNext()) {
            var block = iterator.next();
            if (instance.getBlock(block, Block.Getter.Condition.TYPE).isSolid()) {
                return true;
            }
        }

        return false;
    }

    private PNode newNode(double cost, Pos point, Point goal) {
        tempNode.g = g + cost;
        tempNode.h = PathGenerator.heuristic(point, goal);
        tempNode.setPoint(point);

        var newNode = tempNode;
        tempNode = new PNode(Pos.ZERO, 0, 0, NodeType.WALK, this);

        return newNode;
    }

    static Pos gravitySnap(Instance instance, Point point, BoundingBox boundingBox, double maxFall) {
        point = new Pos(point.blockX() + 0.5, point.blockY(), point.blockZ() + 0.5);

        Chunk c = instance.getChunkAt(point);
        if (c == null) return null;

        for (int axis = 1; axis <= maxFall; ++axis) {
            var iterator = boundingBox.getBlocks(point, BoundingBox.AxisMask.Y, -axis);

            while (iterator.hasNext()) {
                var block = iterator.next();

                if (instance.getBlock(block, Block.Getter.Condition.TYPE).isSolid()) {
                    return Pos.fromPoint(point.withY(block.blockY() + 1));
                }
            }
        }

        return Pos.fromPoint(point.withY(point.y() - maxFall));
    }

    private static boolean canMoveTowards(Instance instance, Pos start, Point end, BoundingBox boundingBox) {
        Point diff = end.sub(start);
        PhysicsResult res = CollisionUtils.handlePhysics(instance, instance.getChunkAt(start), boundingBox, start, Vec.fromPoint(diff), null, false);
        return !res.collisionZ() && !res.collisionY() && !res.collisionX();
    }

    @Override
    public String toString() {
        return "PNode{" +
                "point=" + point +
                ", d=" + (g + h) +
                ", type=" + type +
                '}';
    }

    public Point point() {
        return point;
    }
}
