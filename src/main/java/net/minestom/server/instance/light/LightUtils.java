package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LightUtils {
    private static final Boolean lock = true;

    private static void flushQueue(Instance instance, Set<Point> queue) {
        var updateQueue =
            queue.parallelStream()
                    .map(sectionLocation -> {
                Chunk chunk = instance.getChunk(sectionLocation.blockX(), sectionLocation.blockZ());
                if (chunk == null) return null;

                chunk.invalidate();
                return chunk.getSection(sectionLocation.blockY()).blockLight()
                        .calculateExternal(instance, chunk, sectionLocation.blockY());
            })
            .filter(Objects::nonNull)
            .toList()
            .parallelStream()
            .flatMap(light -> light.flip().stream())
            .collect(Collectors.toSet());

        if (updateQueue.size() > 0) {
            flushQueue(instance, updateQueue);
        }
    }

    public static void relight(Instance instance, Collection<Chunk> chunks) {
        Set<Point> toPropagate = chunks
            .parallelStream()
            .flatMap(chunk -> IntStream
                    .range(chunk.getMinSection(), chunk.getMaxSection())
                    .mapToObj(index -> Map.entry(index, chunk)))
            .map(chunkIndex -> {
                final Chunk chunk = chunkIndex.getValue();
                final int section = chunkIndex.getKey();

                chunk.getSection(section).blockLight().invalidate();

                return new Vec(chunk.getChunkX(), section, chunk.getChunkZ());
            }).collect(Collectors.toSet());

        synchronized (lock) {
            relight(instance, toPropagate);
        }

        chunks.parallelStream()
            .flatMap(chunk -> IntStream
                .range(chunk.getMinSection(), chunk.getMaxSection())
                .mapToObj(index -> Map.entry(index, chunk)))
            .forEach(chunkIndex -> {
                final Chunk chunk = chunkIndex.getValue();
                final int section = chunkIndex.getKey();
                chunk.getSection(section).blockLight().array();
            });
    }

    private static Set<Point> getNearbyRequired(Instance instance, Point point) {
        Set<Point> collected = new HashSet<>();
        for (int x = point.blockX() - 1; x <= point.blockX() + 1; x++) {
            for (int z = point.blockZ() - 1; z <= point.blockZ() + 1; z++) {
                Chunk chunkCheck = instance.getChunk(x, z);
                if (chunkCheck == null) continue;

                for (int y = point.blockY() - 1; y <= point.blockY() + 1; y++) {
                    if (y == point.blockY() && x == point.blockX() && point.blockZ() == z) continue;
                    Point sectionPosition = new Vec(x, y, z);

                    if (sectionPosition.blockY() < chunkCheck.getMaxSection() && sectionPosition.blockY() >= chunkCheck.getMinSection()) {
                        if (chunkCheck.getSection(sectionPosition.blockY()).blockLight().requiresUpdate()) {
                            collected.add(sectionPosition);
                        }
                    }
                }
            }
        }

        return collected;
    }

    private static Set<Point> collectRequiredNearby(Instance instance, Point point) {
        final Set<Point> found = new HashSet<>();
        final ArrayDeque<Point> toCheck = new ArrayDeque<>();

        toCheck.add(point);
        found.add(point);

        while (toCheck.size() > 0 && found.size() < 50) {
            final Point current = toCheck.poll();
            final Set<Point> nearby = getNearbyRequired(instance, current);
            nearby.forEach(p -> {
                if (!found.contains(p)) {
                    found.add(p);
                    toCheck.add(p);
                }
            });
        }

        return found;
    }

    public static void relightSection(Instance instance, int chunkX, int sectionY, int chunkZ) {
        Chunk c = instance.getChunk(chunkX, chunkZ);
        if (c == null) return;

        Section s = c.getSection(sectionY);
        if (!s.blockLight().requiresUpdate()) return;

        Set<Point> collected = getNearbyRequired(instance, new Vec(chunkX, sectionY, chunkZ));

        synchronized (lock) {
            relight(instance, collected);
        }

        c.getSection(sectionY).blockLight().array(); // Free memory
    }

    private static void relight(Instance instance, Set<Point> sections) {
        Set<Point> toPropagate = sections
            .parallelStream()
            .map(chunkIndex -> {
                final Chunk chunk = instance.getChunk(chunkIndex.blockX(), chunkIndex.blockZ());
                final int section = chunkIndex.blockY();
                if (chunk == null) return null;

                return chunk.getSection(section).blockLight().calculateInternal(chunk.getInstance(), chunk.getChunkX(), section, chunk.getChunkZ());
            }).filter(Objects::nonNull)
            .flatMap(lightSet -> lightSet.flip().stream())
            .collect(Collectors.toSet())
            .parallelStream()
            .flatMap(sectionLocation -> {
                final Chunk chunk = instance.getChunk(sectionLocation.blockX(), sectionLocation.blockZ());
                final int section = sectionLocation.blockY();
                if (chunk == null) return Stream.empty();

                final Light light = chunk.getSection(section).blockLight();
                light.calculateExternal(chunk.getInstance(), chunk, section);

                return light.flip().stream();
            }).collect(Collectors.toSet());

        flushQueue(instance, toPropagate);
    }
}
