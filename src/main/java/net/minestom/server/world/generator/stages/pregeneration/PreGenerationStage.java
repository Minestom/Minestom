package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.world.generator.GenerationContext;

public interface PreGenerationStage {
    /**
     * Called for sections in the {@link #getRange() look around} range around the generated section<br>
     * NOTE: This will be called for each section in a cubical radius instead of a spherical one!
     * @param context the generator, used to get results of previous stages and save results of this stage
     * @param sectionX section's X coordinate
     * @param sectionY section's Y coordinate
     * @param sectionZ section's Z coordinate
     */
    void process(GenerationContext context, int sectionX, int sectionY, int sectionZ);

    /**
     * The range in what the generator should look around, e.g. for generating structures
     * @return range > 0
     */
    int getRange();
}
