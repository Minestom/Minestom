package net.minestom.server.world.generator.stages.generation;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.pregeneration.BiomeLayout2DStage;
import net.minestom.server.world.generator.stages.pregeneration.DominantBiomeLayout2DStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BiomeFillStage implements GenerationStage {
    @Override
    public void process(GenerationContext context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
        final DominantBiomeLayout2DStage.Data chunkData = context.getChunkData(DominantBiomeLayout2DStage.Data.class, sectionX, sectionZ);
        int i = 0;
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                final int id = chunkData.biomes()[i++].id();
                for (int y = 0; y < 4; y++) {
                    biomePalette.set(x,y,z, id);
                }
            }
        }
    }

    @Override
    public @NotNull Set<Class<? extends StageData>> getDependencies() {
        return Set.of(DominantBiomeLayout2DStage.Data.class);
    }
}
