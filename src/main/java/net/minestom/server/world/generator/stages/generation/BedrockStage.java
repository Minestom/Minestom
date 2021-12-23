package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.GenerationContext;

public class BedrockStage implements GenerationStage {
    @Override
    public void process(GenerationContext context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        if (sectionY > context.getInstance().getSectionMinY()) return;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                blockCache.setBlock(x, 0, z, Block.BEDROCK);
            }
        }
    }
}
