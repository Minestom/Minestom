package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.StageDataDependent;

public interface GenerationStage extends StageDataDependent {
    void process(GenerationContext context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ);
}
