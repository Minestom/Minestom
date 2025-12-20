package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public sealed interface LightGenerationData {
    int chunkMinY();

    boolean hasSkylight();

    @Nullable LightingChunk get(int x, int z);

    final class SingleChunk implements LightGenerationData {
        private final LightingChunk mainChunk;
        private final int chunkMinY;
        private final boolean hasSkylight;
        private final @Nullable LightingChunk[] chunks;

        public SingleChunk(LightingChunk mainChunk) {
            this.mainChunk = mainChunk;
            this.chunkMinY = mainChunk.getInstance().getCachedDimensionType().minY();
            this.hasSkylight = mainChunk.getInstance().getCachedDimensionType().hasSkylight();
            var posX = get(mainChunk, 1, 0);
            var negX = get(mainChunk, -1, 0);
            var posZ = get(mainChunk, 0, 1);
            var negZ = get(mainChunk, 0, -1);
            var posXposZ = get(mainChunk, 1, 1);
            var posXnegZ = get(mainChunk, 1, -1);
            var negXposZ = get(mainChunk, -1, 1);
            var negXnegZ = get(mainChunk, -1, -1);

            chunks = new LightingChunk[9];
            /*
            678
            345
            012
             */
            chunks[0] = negXnegZ;
            chunks[1] = negZ;
            chunks[2] = posXnegZ;
            chunks[3] = negX;
            chunks[4] = mainChunk;
            chunks[5] = posX;
            chunks[6] = negXposZ;
            chunks[7] = posZ;
            chunks[8] = posXposZ;
        }

        @Override
        public int chunkMinY() {
            return chunkMinY;
        }

        @Override
        public boolean hasSkylight() {
            return hasSkylight;
        }

        @Override
        public @Nullable LightingChunk get(int x, int z) {
            x -= mainChunk.getChunkX();
            z -= mainChunk.getChunkZ();
            if (x < -1 || x > 1 || z < -1 || z > 1) {
                return null;
            }
            return chunks[(z + 1) * 3 + x + 1];
        }

        private static @Nullable LightingChunk get(LightingChunk main, int offsetX, int offsetZ) {
            var chunk = main.getInstance().getChunkManager().getLoadedChunkManaged(main.getChunkX() + offsetX, main.getChunkZ() + offsetZ);
            if (chunk instanceof LightingChunk lighting) return lighting;
            return null;
        }
    }

    final class ManyChunks implements LightGenerationData {
        private final Long2ObjectMap<@UnknownNullability LightingChunk> chunks = new Long2ObjectOpenHashMap<>();
        private final int chunkMinY;
        private final boolean hasSkylight;

        public ManyChunks(Collection<? extends Chunk> chunks) {
            if (chunks.isEmpty()) throw new IllegalArgumentException("Chunks collection must not be empty");
            Instance instance = null;
            for (Chunk chunk : chunks) {
                if (instance == null) instance = Objects.requireNonNull(chunk.getInstance());
                else if (instance != chunk.getInstance())
                    throw new IllegalArgumentException("All chunks must be of the same instance");
                if (!(chunk instanceof LightingChunk lightingChunk))
                    continue; // Ignore. Do we want to throw an exception?
                this.chunks.put(CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ()), lightingChunk);
            }
            // We also need all neighbors for every chunk (if available)
            for (LightingChunk chunk : List.copyOf(this.chunks.values())) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) continue;
                        var index = CoordConversion.chunkIndex(chunk.getChunkX() + x, chunk.getChunkZ() + z);
                        if (this.chunks.containsKey(index)) continue;
                        var neighbor = instance.getChunkManager().getLoadedChunkManaged(chunk.getChunkX() + x, chunk.getChunkZ() + z);
                        if (neighbor instanceof LightingChunk lighting) {
                            this.chunks.put(index, lighting);
                        }
                    }
                }
            }
            this.chunkMinY = instance.getCachedDimensionType().minY();
            this.hasSkylight = instance.getCachedDimensionType().hasSkylight();
        }

        @Override
        public int chunkMinY() {
            return chunkMinY;
        }

        @Override
        public boolean hasSkylight() {
            return hasSkylight;
        }

        @Override
        public @Nullable LightingChunk get(int x, int z) {
            return chunks.get(CoordConversion.chunkIndex(x, z));
        }
    }
}
