package demo.generator;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.generation.GenerationStage;

import java.util.Collection;

public class DebugGridStage implements GenerationStage {
    private static final Block[] blocks = Block.values().stream().map(Block::possibleStates).flatMap(Collection::stream).toArray(Block[]::new);
    private static final int widthX = (int) Math.sqrt(blocks.length);


    @Override
    public void process(GenerationContext context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        if (sectionY != -3 || sectionX < 0 || sectionZ < 0 || sectionZ * 16 > blocks.length/widthX) return;
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                int index = (sectionX*8+x) * widthX + sectionZ*8+z;
                if (index < blocks.length) {
                    blockCache.setBlock(x*2,0, z*2, blocks[index]);
                }
            }
        }
    }
}
