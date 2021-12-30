package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.BiomeStages;
import net.minestom.server.world.generator.stages.pregeneration.HeightMapStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TerrainStage implements GenerationStage {
    @Override
    public void process(GenerationContext context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        final HeightMapStage.HeightMapData heightMapData = context.getChunkData(HeightMapStage.HeightMapData.class, sectionX, sectionZ);
        final BiomeStages.ChunkBiomeGrid chunkBiomeGrid = context.getChunkData(BiomeStages.ChunkBiomeGrid.class, sectionX, sectionZ);
        int i = 0;
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            int globalX = sectionX * Chunk.CHUNK_SIZE_X + x;
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                int height = heightMapData.heightMap()[i++];
                int toY = height > (sectionY + 1) * Chunk.CHUNK_SECTION_SIZE ? 15 : Math.abs(height % Chunk.CHUNK_SECTION_SIZE);
                int globalZ = sectionZ * Chunk.CHUNK_SIZE_Z + z;
                for (int y = 0; y <= toY; y++) {
                    int globalY = sectionY * Chunk.CHUNK_SECTION_SIZE + y;
                    blockCache.setBlock(x,y,z,chunkBiomeGrid.biomes()[(x/4)*4+(z/4)].terrainBlocks().getBlock(globalX, globalY, globalZ, height));
                }
            }
        }
    }

    @Override
    public @NotNull Set<Class<? extends StageData>> getDependencies() {
        return Set.of(HeightMapStage.HeightMapData.class, BiomeStages.ChunkBiomeGrid.class);
    }
}
