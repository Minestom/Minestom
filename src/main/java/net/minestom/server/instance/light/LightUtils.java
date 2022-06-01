package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LightUtils {
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
        synchronized (instance) {
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

            relight(instance, toPropagate);

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
    }

    public static void relightSection(Instance instance, int chunkX, int sectionY, int chunkZ) {
        synchronized (instance) {
            Set<Point> collected = new HashSet<>();
            for (int x = chunkX - 1; x <= chunkX + 1; x++) {
                for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                    for (int y = sectionY - 1; y <= sectionY + 1; y++) {
                        Chunk chunkCheck = instance.getChunk(x, z);
                        Point sectionPosition = new Vec(x, y, z);
                        if (chunkCheck == null) continue;

                        if (sectionPosition.blockY() < chunkCheck.getMaxSection() && sectionPosition.blockY() >= chunkCheck.getMinSection()) {
                            collected.add(new Vec(x, y, z));
                        }
                    }
                }
            }

            relight(instance, collected);
        }
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
