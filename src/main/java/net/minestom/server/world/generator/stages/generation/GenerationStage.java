package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.world.generator.GenerationContext;

public interface GenerationStage {
    void process(GenerationContext context, Palette blockPalette, Palette biomePalette, int sectionX, int sectionY, int sectionZ);
}
