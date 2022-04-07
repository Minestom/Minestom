package net.minestom.server.entity.pathfinding.engine.astar;

import it.unimi.dsi.fastutil.objects.Object2DoubleFunction;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class AStarPathfinder {

    private static final double delta = 0.01;

    public static @Nullable Queue<Point> findPath(
            Point start,
            Point goal,
            double step,
            PathfindingEngine.CostProvider cost,
            PathfindingEngine.BlockedPredicate isBlocked
    ) {
        Comparator<Point> distanceCost = Comparator.comparingDouble(p ->
                p.distance(start) +
                p.distance(goal) +
                cost.getCost(p, p)
        );
        // The queue of nodes to be evaluated next
        Queue<Point> next = new PriorityQueue<>(distanceCost);
        Set<Point> nextSet = new HashSet<>();
        next.add(start);

        // The set of nodes already evaluated
        Set<Point> closedSet = new HashSet<>();

        // The map from each node to its parent node
        Map<Point, Point> cameFrom = new HashMap<>();

        while (!next.isEmpty()) {
            Point current = next.remove();
            nextSet.remove(current);

            // TODO: Remove this debug
            ServerPacket packet = ParticleCreator.createParticlePacket(
                    Particle.FLAME,
                    current.x(), current.y(), current.z(),
                    0, 0, 0,
                    1
            );
            PacketUtils.sendGroupedPacket(MinecraftServer.getConnectionManager().getOnlinePlayers(), packet);

            // Return if the current node is the goal
            if (current.distance(goal) - delta <= step) {
                return reconstructPath(cameFrom, current);
            }

            // Else, look at the neighbors
            for (Point neighbor : neighbors(current, step)) {
                // If the neighbor is already evaluated, or scheduled for evaluation, skip it
                if (closedSet.contains(neighbor) || nextSet.contains(neighbor)) {
                    continue;
                }

                // If the neighbor is not walkable, skip it
                if (isBlocked.isBlocked(neighbor)) {
                    continue;
                }

                // Else, add it to the queue
                next.add(neighbor);
                nextSet.add(neighbor);
                cameFrom.put(neighbor, current);
            }

            // Mark the current node as visited
            closedSet.add(current);
        }

        // No path found
        return null;
    }

    private static Queue<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        Deque<Point> path = new ArrayDeque<>();
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.addFirst(current);
        }
        return path;
    }

    private static Point[] neighbors(Point point, double step) {
        return new Point[] {
                // Direct neighbors
                point.add(step, 0, 0),
                point.add(-step, 0, 0),
                point.add(0, step, 0),
                point.add(0, -step, 0),
                point.add(0, 0, step),
                point.add(0, 0, -step),

                // Diagonal neighbors
                point.add(step, step, 0),
                point.add(-step, -step, 0),
                point.add(step, -step, 0),
                point.add(-step, step, 0),
                point.add(0, step, step),
                point.add(0, -step, -step),
                point.add(step, 0, step),
                point.add(-step, 0, -step),

                // Diagonal Diagonal neighbors
                point.add(step, step, step),
                point.add(-step, -step, -step),
                point.add(step, -step, -step),
                point.add(-step, step, -step),
                point.add(step, step, -step),
                point.add(-step, -step, step),
                point.add(step, -step, step),
                point.add(-step, step, step)
        };
    }
}
