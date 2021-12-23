package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.GenerationContext;

public class BiomeFillStage implements GenerationStage {
    @Override
    public void process(GenerationContext context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    biomePalette.set(x,y,z, 0);
                }
            }
        }
    }
}
