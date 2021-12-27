package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.BlockPool;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.pregeneration.DominantBiomeLayout2DStage;
import net.minestom.server.world.generator.stages.pregeneration.HeightMapStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TerrainStage implements GenerationStage {
    private final Map<Biome, BlockPool> biomeTerrainBlockPool;
    private final BlockPool defaultBlockPool;

    public TerrainStage(Map<Biome, BlockPool> biomeTerrainBlockPool, @Nullable BlockPool defaultBlockPool) {
        this.biomeTerrainBlockPool = biomeTerrainBlockPool;
        this.defaultBlockPool = Objects.requireNonNullElseGet(defaultBlockPool, () -> new BlockPool(((x, y, z) -> 0)));
    }

    @Override
    public void process(GenerationContext context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        final HeightMapStage.Data chunkData = context.getChunkData(HeightMapStage.Data.class, sectionX, sectionZ);
        final DominantBiomeLayout2DStage.Data biomeData = context.getChunkData(DominantBiomeLayout2DStage.Data.class, sectionX, sectionZ);
        int i = 0;
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            int globalX = sectionX * Chunk.CHUNK_SIZE_X + x;
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                int height = chunkData.heightMap()[i++];
                int toY = height > (sectionY + 1) * Chunk.CHUNK_SECTION_SIZE ? 15 : Math.abs(height % Chunk.CHUNK_SECTION_SIZE);
                int globalZ = sectionZ * Chunk.CHUNK_SIZE_Z + z;
                for (int y = 0; y <= toY; y++) {
                    int globalY = sectionY * Chunk.CHUNK_SECTION_SIZE + y;
                    blockCache.setBlock(x,y,z,biomeTerrainBlockPool.getOrDefault(biomeData.biomes()[(x/4)*4+(z/4)], defaultBlockPool).getBlock(globalX, globalY, globalZ, height));
                }
            }
        }
    }

    @Override
    public @NotNull Set<Class<? extends StageData>> getDependencies() {
        return Set.of(HeightMapStage.Data.class, DominantBiomeLayout2DStage.Data.class);
    }
}
