package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.pregeneration.HeightMapStage;

public class TerrainStage implements GenerationStage {
    @Override
    public void process(GenerationContext<?> context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        final int height = context.getChunkData(HeightMapStage.class, sectionX, sectionZ).getHeight();
        if (Math.ceil(height / 16d) >= sectionY) {
            int h = height > sectionY * 16 ? 16 : sectionY * 16 - height;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < h; y++) {
                    for (int z = 0; z < 16; z++) {
                        blockCache.setBlock(x,y,z, Block.STONE);
                    }
                }
            }
        }
    }
}
