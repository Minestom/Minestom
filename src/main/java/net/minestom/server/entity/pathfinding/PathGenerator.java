package net.minestom.server.entity.pathfinding;

import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PathGenerator {
    private static final ExecutorService pool = Executors.newWorkStealingPool();
    private static final PNode repathNode = new PNode(Pos.ZERO, 0, 0, PNode.NodeType.REPATH, null);

    public static double heuristic (Point node, Point target) {
        return node.distance(target);
    }

    static Comparator<PNode> pNodeComparator = (s1, s2) -> (int) (((s1.g + s1.h) - (s2.g + s2.h)) * 1000);
    public static PPath generate(Instance instance, Pos orgStart, Point orgTarget, double closeDistance, double maxDistance, double pathVariance, BoundingBox boundingBox, PPath.PathfinderCapabilities capabilities, boolean startOnGround, Runnable onComplete) {
        Pos start = (capabilities.type() == PPath.PathfinderType.AQUATIC
                || capabilities.type() == PPath.PathfinderType.FLYING
                || (capabilities.type() == PPath.PathfinderType.AMPHIBIOUS && instance.getBlock(orgStart).compare(Block.WATER))
                || startOnGround)
                    ? orgStart
                    : PNode.gravitySnap(instance, orgStart, boundingBox, 100);

        Pos target = (capabilities.type() == PPath.PathfinderType.AQUATIC
                || capabilities.type() == PPath.PathfinderType.FLYING
                || (capabilities.type() == PPath.PathfinderType.AMPHIBIOUS && instance.getBlock(orgTarget).compare(Block.WATER)))
                    ? Pos.fromPoint(orgTarget)
                    : PNode.gravitySnap(instance, orgTarget, boundingBox, 100);

        if (start == null || target == null) return null;

        PPath path = new PPath(maxDistance, pathVariance, capabilities, onComplete);
        pool.submit(() -> computePath(instance, start, target, closeDistance, maxDistance, pathVariance, boundingBox, path));

        return path;
    }

    private static void computePath(Instance instance, Pos start, Pos target, double closeDistance, double maxDistance, double pathVariance, BoundingBox boundingBox, PPath path) {
        double closestDistance = Double.MAX_VALUE;
        double straightDistance = heuristic(start, target);
        int maxSize = (int) Math.floor(maxDistance * 10);

        closeDistance = Math.max(0.8, closeDistance);
        List<PNode> closestFoundNodes = List.of();

        PNode pStart = new PNode(start, 0, heuristic(start, target), PNode.NodeType.WALK, null);

        ObjectHeapPriorityQueue<PNode> open = new ObjectHeapPriorityQueue<>(pNodeComparator);
        open.enqueue(pStart);

        Set<PNode> closed = new ObjectOpenHashBigSet<>(maxSize);

        while (!open.isEmpty() && closed.size() < maxSize) {
            if (path.getState() == PPath.PathState.TERMINATING) {
                path.setState(PPath.PathState.TERMINATED);
                return;
            }

            PNode current = open.dequeue();

            var chunk = instance.getChunkAt(current.point);
            if (chunk == null) continue;
            if (!chunk.isLoaded()) continue;

            if (((current.g + current.h) - straightDistance) > pathVariance) continue;
            if (!withinDistance(current.point, start, maxDistance)) continue;
            if (withinDistance(current.point, target, closeDistance)) {
                open.enqueue(current);
                break;
            }

            if (current.h < closestDistance) {
                closestDistance = current.h;
                closestFoundNodes = List.of(current);
            }

            var found = current.getNearby(instance, closed, target, boundingBox, path.capabilities());
            found.forEach(p -> {
                if (p.point.distance(start) <= maxDistance) {
                    open.enqueue(p);
                    closed.add(p);
                }
            });
        }

        PNode current = open.isEmpty() ? null : open.dequeue();

        if (current == null || open.isEmpty() || !withinDistance(current.point, target, closeDistance)) {
            if (closestFoundNodes.isEmpty()) {
                path.setState(PPath.PathState.INVALID);
                return;
            }

            current = closestFoundNodes.get(0);

            if (!open.isEmpty()) {
                repathNode.parent = current;
                current = repathNode;
            }
        }

        while (current.parent != null) {
            path.getNodes().add(current);
            current = current.parent;
        }

        Collections.reverse(path.getNodes());

        if (path.getCurrentType() == PNode.NodeType.REPATH) {
            path.setState(PPath.PathState.INVALID);
            path.getNodes().clear();
            return;
        }

        var lastNode = path.getNodes().get(path.getNodes().size() - 1);
        if (lastNode.point.distance(target) > closeDistance) {
            path.setState(PPath.PathState.BEST_EFFORT);
            return;
        }

        PNode pEnd = new PNode(target, 0, 0, PNode.NodeType.WALK, null);
        path.getNodes().add(pEnd);
        path.setState(PPath.PathState.COMPUTED);
    }

    private static boolean withinDistance(Pos point, Pos target, double closeDistance) {
        return point.distanceSquared(target) < (closeDistance * closeDistance);
    }
}
