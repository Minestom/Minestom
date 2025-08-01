package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApiStatus.Internal
public final class ChunkLight {
    private static final ExecutorService POOL = Executors.newWorkStealingPool();

    enum LightType {
        SKY,
        BLOCK
    }

    enum QueueType {
        INTERNAL,
        EXTERNAL
    }

    static final Set<Key> DIFFUSE_SKY_LIGHT = Set.of(
            Block.COBWEB.key(),
            Block.ICE.key(),
            Block.HONEY_BLOCK.key(),
            Block.SLIME_BLOCK.key(),
            Block.WATER.key(),
            Block.ACACIA_LEAVES.key(),
            Block.AZALEA_LEAVES.key(),
            Block.BIRCH_LEAVES.key(),
            Block.DARK_OAK_LEAVES.key(),
            Block.FLOWERING_AZALEA_LEAVES.key(),
            Block.JUNGLE_LEAVES.key(),
            Block.CHERRY_LEAVES.key(),
            Block.OAK_LEAVES.key(),
            Block.SPRUCE_LEAVES.key(),
            Block.SPAWNER.key(),
            Block.BEACON.key(),
            Block.END_GATEWAY.key(),
            Block.CHORUS_PLANT.key(),
            Block.CHORUS_FLOWER.key(),
            Block.FROSTED_ICE.key(),
            Block.SEAGRASS.key(),
            Block.TALL_SEAGRASS.key(),
            Block.LAVA.key()
    );

    static boolean checkSkyOcclusion(Block block) {
        if (block == Block.AIR) return false;
        if (DIFFUSE_SKY_LIGHT.contains(block.key())) return true;

        Shape shape = block.registry().collisionShape();
        boolean occludesTop = Block.AIR.registry().collisionShape().isOccluded(shape, BlockFace.TOP);
        boolean occludesBottom = Block.AIR.registry().collisionShape().isOccluded(shape, BlockFace.BOTTOM);

        return occludesBottom || occludesTop;
    }

    private static Set<Chunk> flushQueue(Instance instance, Set<Point> queue, LightType type, QueueType queueType) {
        Set<Light> sections = ConcurrentHashMap.newKeySet();
        Set<Point> newQueue = ConcurrentHashMap.newKeySet();

        Set<Chunk> responseChunks = ConcurrentHashMap.newKeySet();
        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        Light.LightLookup lightLookup = (x, y, z) -> {
            Chunk chunk = instance.getChunk(x, z);
            if (chunk == null) return null;
            ChunkImpl impl = (ChunkImpl) chunk;
            if (!impl.hasLightEngine()) return null;
            if (y - chunk.getMinSection() < 0 || y - chunk.getMaxSection() >= 0) return null;
            final Section section = chunk.getSection(y);
            return switch (type) {
                case BLOCK -> section.blockLight();
                case SKY -> section.skyLight();
            };
        };

        Light.PaletteLookup paletteLookup = (x, y, z) -> {
            Chunk chunk = instance.getChunk(x, z);
            if (chunk == null) return null;
            ChunkImpl impl = (ChunkImpl) chunk;
            if (!impl.hasLightEngine()) return null;
            if (y - chunk.getMinSection() < 0 || y - chunk.getMaxSection() >= 0) return null;
            return chunk.getSection(y).blockPalette();
        };

        for (Point point : queue) {
            Chunk chunk = instance.getChunk(point.blockX(), point.blockZ());
            if (!(chunk instanceof ChunkImpl chunkImpl)) continue;

            Section section = chunk.getSection(point.blockY());
            responseChunks.add(chunk);

            Light light = switch (type) {
                case BLOCK -> section.blockLight();
                case SKY -> section.skyLight();
            };

            final Palette blockPalette = section.blockPalette();
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                try {
                    final Set<Point> toAdd = switch (queueType) {
                        case INTERNAL -> light.calculateInternal(blockPalette,
                                chunk.getChunkX(), point.blockY(), chunk.getChunkZ(),
                                chunkImpl.getOcclusionMap(), instance.getCachedDimensionType().maxY(),
                                lightLookup);
                        case EXTERNAL -> light.calculateExternal(blockPalette,
                                Light.getNeighbors(instance, chunk, point.blockY()),
                                lightLookup, paletteLookup);
                    };

                    sections.add(light);

                    light.flip();
                    newQueue.addAll(toAdd);
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }, POOL);

            tasks.add(task);
        }

        tasks.forEach(CompletableFuture::join);

        if (!newQueue.isEmpty()) {
            var newResponse = flushQueue(instance, newQueue, type, QueueType.EXTERNAL);
            responseChunks.addAll(newResponse);
        }

        return responseChunks;
    }

    /**
     * Forces a relight of the specified chunks.
     * <p>
     * This method is used to force a relight of the specified chunks.
     * <p>
     * This method is thread-safe and can be called from any thread.
     *
     * @param instance the instance
     * @param chunks   the chunks to relight
     * @return the chunks which have been relighted
     */
    public static List<Chunk> relight(Instance instance, Collection<Chunk> chunks) {
        Set<Point> sections = new HashSet<>();
        synchronized (instance) {
            for (Chunk chunk : chunks) {
                ChunkImpl impl = (ChunkImpl) chunk;
                if (!impl.hasLightEngine()) continue;
                for (int sectionIndex = chunk.getMinSection(); sectionIndex < chunk.getMaxSection(); sectionIndex++) {
                    Section section = chunk.getSection(sectionIndex);
                    section.blockLight().invalidate();
                    section.skyLight().invalidate();
                    sections.add(new BlockVec(chunk.getChunkX(), sectionIndex, chunk.getChunkZ()));
                }
                chunk.invalidate();
            }

            // Expand the sections to include nearby sections
            var blockSections = new HashSet<Point>();
            for (Point point : sections) {
                blockSections.addAll(getNearbyRequired(instance, point, LightType.BLOCK));
            }

            var skySections = new HashSet<Point>();
            for (Point point : sections) {
                skySections.addAll(getNearbyRequired(instance, point, LightType.SKY));
            }

            relight(instance, blockSections, LightType.BLOCK);
            relight(instance, skySections, LightType.SKY);

            var chunksToRelight = new HashSet<Chunk>();
            for (Point point : blockSections) {
                chunksToRelight.add(instance.getChunk(point.blockX(), point.blockZ()));
            }

            for (Point point : skySections) {
                chunksToRelight.add(instance.getChunk(point.blockX(), point.blockZ()));
            }

            return new ArrayList<>(chunksToRelight);
        }
    }

    private static Set<Point> getNearbyRequired(Instance instance, Point point, LightType type) {
        Set<Point> collected = new HashSet<>();
        collected.add(point);

        int highestRegionPoint = instance.getCachedDimensionType().minY() - 1;

        for (int x = point.blockX() - 1; x <= point.blockX() + 1; x++) {
            for (int z = point.blockZ() - 1; z <= point.blockZ() + 1; z++) {
                Chunk chunkCheck = instance.getChunk(x, z);
                if (chunkCheck == null) continue;
                ChunkImpl impl = (ChunkImpl) chunkCheck;
                if (impl.hasLightEngine()) {
                    // Ensure heightmap is calculated before taking values from it
                    impl.getOcclusionMap();
                    highestRegionPoint = Math.max(highestRegionPoint, impl.highestBlock);
                }
            }
        }

        for (int x = point.blockX() - 1; x <= point.blockX() + 1; x++) {
            for (int z = point.blockZ() - 1; z <= point.blockZ() + 1; z++) {
                Chunk chunkCheck = instance.getChunk(x, z);
                if (chunkCheck == null) continue;

                for (int y = point.blockY() - 1; y <= point.blockY() + 1; y++) {
                    final Point sectionPosition = new BlockVec(x, y, z);
                    int sectionHeight = instance.getCachedDimensionType().minY() + 16 * y;
                    if ((sectionHeight + 16) > highestRegionPoint && type == LightType.SKY) continue;

                    if (sectionPosition.blockY() < chunkCheck.getMaxSection() && sectionPosition.blockY() >= chunkCheck.getMinSection()) {
                        Section s = chunkCheck.getSection(sectionPosition.blockY());
                        if (type == LightType.BLOCK && !s.blockLight().requiresUpdate()) continue;
                        if (type == LightType.SKY && !s.skyLight().requiresUpdate()) continue;
                        collected.add(sectionPosition);
                    }
                }
            }
        }

        return collected;
    }

    private static Set<Point> collectRequiredNearby(Instance instance, Point point, LightType type) {
        final Set<Point> found = new HashSet<>();
        final ArrayDeque<Point> toCheck = new ArrayDeque<>();

        toCheck.add(point);
        found.add(point);

        while (!toCheck.isEmpty()) {
            final Point current = toCheck.poll();
            final Set<Point> nearby = getNearbyRequired(instance, current, type);
            nearby.forEach(p -> {
                if (!found.contains(p)) {
                    found.add(p);
                    toCheck.add(p);
                }
            });
        }

        return found;
    }

    static Set<Chunk> relightSection(Instance instance, int sectionX, int sectionY, int sectionZ) {
        var res = new HashSet<>(relightSection(instance, sectionX, sectionY, sectionZ, LightType.BLOCK));
        res.addAll(relightSection(instance, sectionX, sectionY, sectionZ, LightType.SKY));
        return res;
    }

    static Set<Chunk> relightSection(Instance instance, int sectionX, int sectionY, int sectionZ, LightType type) {
        Chunk c = instance.getChunk(sectionX, sectionZ);
        if (c == null) return Set.of();
        ChunkImpl impl = (ChunkImpl) c;
        if (!impl.hasLightEngine()) return Set.of();
        synchronized (instance) {
            Set<Point> collected = collectRequiredNearby(instance, new BlockVec(sectionX, sectionY, sectionZ), type);
            return relight(instance, collected, type);
        }
    }

    static Set<Chunk> relight(Instance instance, Set<Point> queue, LightType type) {
        return flushQueue(instance, queue, type, QueueType.INTERNAL);
    }
}
