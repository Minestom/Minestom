package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.GenerationContext;

public interface GenerationStage {
    void process(GenerationContext<?> context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ);
}
