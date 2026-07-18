package net.minestom.server.instance.light;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Computes light over an arbitrary section-addressable world.
 * <p>
 * {@link net.minestom.server.instance.LightingChunk} drives this engine for instances. Other worlds (virtual or
 * packet-based worlds, world diffs, schematic previews) implement {@link WorldView} and get the same propagation
 * without materializing chunks.
 * <p>
 * {@link #relight(WorldView, Set, Type)} recomputes the given sections, then propagates outward wave by wave until
 * no section border changes. Each wave's sections are computed in parallel.
 */
public final class LightEngine {

    private static final ExecutorService POOL = Executors.newWorkStealingPool();
    private static final Direction[] DIRECTIONS = Direction.values();

    /** Blocks that occlude sky light despite a non-occluding collision shape. */
    private static final Set<Key> DIFFUSE_SKY_LIGHT = Set.of(
            Block.COBWEB.key(), Block.ICE.key(), Block.HONEY_BLOCK.key(), Block.SLIME_BLOCK.key(),
            Block.WATER.key(), Block.ACACIA_LEAVES.key(), Block.AZALEA_LEAVES.key(), Block.BIRCH_LEAVES.key(),
            Block.DARK_OAK_LEAVES.key(), Block.FLOWERING_AZALEA_LEAVES.key(), Block.JUNGLE_LEAVES.key(),
            Block.CHERRY_LEAVES.key(), Block.OAK_LEAVES.key(), Block.SPRUCE_LEAVES.key(), Block.SPAWNER.key(),
            Block.BEACON.key(), Block.END_GATEWAY.key(), Block.CHORUS_PLANT.key(), Block.CHORUS_FLOWER.key(),
            Block.FROSTED_ICE.key(), Block.SEAGRASS.key(), Block.TALL_SEAGRASS.key(), Block.LAVA.key());

    private LightEngine() {
    }

    public enum Type {
        BLOCK,
        SKY
    }

    /**
     * A section-addressable world for the engine to light. Sections are addressed by chunk coordinates plus an
     * absolute section Y. Return {@code null} for sections that do not exist (unloaded, out of range).
     */
    public interface WorldView {
        /**
         * The {@link Light} state of a section, or {@code null}.
         * <p>
         * The engine mutates lights of sections it {@link #recomputes(int, int, int) recomputes}; other sections
         * are only read for border seeds.
         */
        @Nullable Light light(Type type, int chunkX, int sectionY, int chunkZ);

        /**
         * The block palette of a section, or {@code null}.
         */
        @Nullable Palette palette(int chunkX, int sectionY, int chunkZ);

        /**
         * The sky-occlusion heightmap of a column, or {@code null}. Each entry is one above the highest
         * {@link #checkSkyOcclusion(Block) sky-occluding} block of that column, indexed {@code z << 4 | x}.
         */
        int @Nullable [] occlusionMap(int chunkX, int chunkZ);

        /**
         * The world's maximum Y (exclusive).
         */
        int maxY();

        /**
         * Whether the engine may recompute this section's light. Border changes never propagate past a
         * non-recomputing section. Defaults to every section with a light.
         */
        default boolean recomputes(int chunkX, int sectionY, int chunkZ) {
            return light(Type.BLOCK, chunkX, sectionY, chunkZ) != null;
        }

        /**
         * Runs {@code op} under the column's read lock, if the world has one.
         */
        default <T> T locked(int chunkX, int chunkZ, Supplier<T> op) {
            return op.get();
        }
    }

    /**
     * Recomputes {@code sections} and every section their border changes reach, until stable.
     * <p>
     * Queue the whole dirty neighborhood - a changed section plus its adjacent sections: an internal pass resets
     * its neighbors' propagation, exactly like instance relights collect their surroundings.
     *
     * @return every section that recomputed; their {@link Light#array()} contents are fresh
     */
    public static Set<Point> relight(WorldView view, Set<Point> sections, Type type) {
        Set<Point> touched = new HashSet<>();
        Set<Point> queue = Set.copyOf(sections);
        boolean internal = true;
        while (!queue.isEmpty()) {
            // normalized: callers may queue any Point implementation
            for (Point point : queue) touched.add(new BlockVec(point.blockX(), point.blockY(), point.blockZ()));
            queue = wave(view, queue, type, internal);
            internal = false;
        }
        return touched;
    }

    private static Set<Point> wave(WorldView view, Set<Point> queue, Type type, boolean internal) {
        Set<Point> newQueue = ConcurrentHashMap.newKeySet();
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        Light.LightLookup lightLookup = (x, y, z) -> view.light(type, x, y, z);
        Light.PaletteLookup paletteLookup = view::palette;
        for (Point point : queue) {
            final int x = point.blockX(), y = point.blockY(), z = point.blockZ();
            Light light = view.light(type, x, y, z);
            Palette palette = view.palette(x, y, z);
            if (light == null || palette == null) continue;
            tasks.add(CompletableFuture.runAsync(() -> {
                try {
                    Set<Point> toAdd = view.locked(x, z, () -> {
                        if (!internal) {
                            return light.calculateExternal(palette, neighbors(view, x, y, z), lightLookup, paletteLookup);
                        }
                        int[] occlusion = view.occlusionMap(x, z); // under the lock: implementations may compute it lazily
                        if (occlusion == null && type == Type.SKY) return Set.<Point>of();
                        return light.calculateInternal(palette, x, y, z, occlusion, view.maxY(), lightLookup);
                    });
                    light.flip();
                    for (Point neighbor : toAdd) {
                        if (view.recomputes(neighbor.blockX(), neighbor.blockY(), neighbor.blockZ())) {
                            newQueue.add(neighbor);
                        }
                    }
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }, POOL));
        }
        tasks.forEach(CompletableFuture::join);
        return newQueue;
    }

    private static Point[] neighbors(WorldView view, int chunkX, int sectionY, int chunkZ) {
        Point[] links = new Point[DIRECTIONS.length];
        for (Direction direction : DIRECTIONS) {
            final int x = chunkX + direction.normalX();
            final int y = sectionY + direction.normalY();
            final int z = chunkZ + direction.normalZ();
            if (view.palette(x, y, z) == null) continue;
            links[direction.ordinal()] = new BlockVec(x, y, z);
        }
        return links;
    }

    /** Whether {@code block} occludes sky light - the test behind a {@link WorldView#occlusionMap} column walk. */
    public static boolean checkSkyOcclusion(Block block) {
        if (block == Block.AIR) return false;
        if (DIFFUSE_SKY_LIGHT.contains(block.key())) return true;
        Shape shape = block.registry().occlusionShape();
        boolean occludesTop = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.TOP);
        boolean occludesBottom = Block.AIR.registry().occlusionShape().isOccluded(shape, BlockFace.BOTTOM);
        return occludesBottom || occludesTop;
    }
}
