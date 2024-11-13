package net.minestom.server.entity.pathfinding;

import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class PathGenerator {
    private static final Comparator<PNode> pNodeComparator = (s1, s2) -> (int) (((s1.g() + s1.h()) - (s2.g() + s2.h())) * 1000);

    public static @NotNull PPath generate(Block.@NotNull Getter getter, @NotNull Pos orgStart, @NotNull Point orgTarget,
                                          double closeDistance, double maxDistance, double pathVariance,
                                          @NotNull BoundingBox boundingBox, boolean isOnGround, @NotNull NodeGenerator generator,
                                          @Nullable Runnable onComplete) {
        final Point start = (!isOnGround && generator.hasGravitySnap())
                ? orgStart.withY(generator.gravitySnap(getter, orgStart.x(), orgStart.y(), orgStart.z(), boundingBox, 100).orElse(orgStart.y()))
                : orgStart;

        final Point target = (generator.hasGravitySnap())
                ? orgTarget.withY(generator.gravitySnap(getter, orgTarget.x(), orgTarget.y(), orgTarget.z(), boundingBox, 100).orElse(orgTarget.y()))
                : Pos.fromPoint(orgTarget);

        PPath path = new PPath(maxDistance, pathVariance, onComplete);
        computePath(getter, start, target, closeDistance, maxDistance, pathVariance, boundingBox, path, generator);
        return path;
    }

    private static PNode buildRepathNode(PNode parent) {
        return new PNode(0, 0, 0, 0, 0, PNode.Type.REPATH, parent);
    }

    private static void computePath(Block.Getter getter, Point start, Point target,
                                    double closeDistance, double maxDistance, double pathVariance,
                                    BoundingBox boundingBox, PPath path, NodeGenerator generator) {
        double closestDistance = Double.MAX_VALUE;
        double straightDistance = generator.heuristic(start, target);
        int maxSize = (int) Math.floor(maxDistance * 10);

        closeDistance = Math.max(0.8, closeDistance);
        List<PNode> closestFoundNodes = List.of();

        PNode pStart = new PNode(start, 0, generator.heuristic(start, target), PNode.Type.WALK, null);

        ObjectHeapPriorityQueue<PNode> open = new ObjectHeapPriorityQueue<>(pNodeComparator);
        open.enqueue(pStart);

        Set<PNode> closed = new ObjectOpenHashBigSet<>(maxSize);

        while (!open.isEmpty() && closed.size() < maxSize) {
            if (path.getState() == PPath.State.TERMINATING) {
                path.setState(PPath.State.TERMINATED);
                return;
            }

            PNode current = open.dequeue();

            //var chunk = instance.getChunkAt(current.x(), current.z());
            //if (chunk == null) continue;
            //if (!chunk.isLoaded()) continue;

            if (((current.g() + current.h()) - straightDistance) > pathVariance) continue;
            if (!withinDistance(current, start, maxDistance)) continue;
            if (withinDistance(current, target, closeDistance)) {
                open.enqueue(current);
                break;
            }

            if (current.h() < closestDistance) {
                closestDistance = current.h();
                closestFoundNodes = List.of(current);
            }

            Collection<? extends PNode> found = generator.getWalkable(getter, closed, current, target, boundingBox);
            found.forEach(p -> {
                if (getDistanceSquared(p.x(), p.y(), p.z(), start) <= (maxDistance * maxDistance)) {
                    open.enqueue(p);
                    closed.add(p);
                }
            });
        }

        PNode current = open.isEmpty() ? null : open.dequeue();

        if (current == null || !withinDistance(current, target, closeDistance)) {
            if (closestFoundNodes.isEmpty()) {
                path.setState(PPath.State.INVALID);
                return;
            }

            current = closestFoundNodes.getFirst();

            if (!open.isEmpty()) {
                current = buildRepathNode(current);
            }
        }

        while (current.parent() != null) {
            path.getNodes().add(current);
            current = current.parent();
        }

        Collections.reverse(path.getNodes());

        if (path.getCurrentType() == PNode.Type.REPATH) {
            path.setState(PPath.State.INVALID);
            path.getNodes().clear();
            return;
        }

        if (path.getNodes().isEmpty()) {
            path.setState(PPath.State.INVALID);
            return;
        }

        var lastNode = path.getNodes().getLast();
        if (getDistanceSquared(lastNode.x(), lastNode.y(), lastNode.z(), target) > (closeDistance * closeDistance)) {
            path.setState(PPath.State.BEST_EFFORT);
            return;
        }

        PNode pEnd = new PNode(target, 0, 0, PNode.Type.WALK, null);
        path.getNodes().add(pEnd);
        path.setState(PPath.State.COMPUTED);
    }

    private static boolean withinDistance(PNode point, Point target, double closeDistance) {
        return getDistanceSquared(point.x(), point.y(), point.z(), target) < (closeDistance * closeDistance);
    }

    private static double getDistanceSquared(double x, double y, double z, Point target) {
        double dx = x - target.x();
        double dy = y - target.y();
        double dz = z - target.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
