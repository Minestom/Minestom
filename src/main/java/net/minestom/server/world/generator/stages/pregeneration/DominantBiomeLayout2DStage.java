package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.GenerationContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DominantBiomeLayout2DStage implements PreGenerationStage<DominantBiomeLayout2DStage.Data> {

    @Override
    public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
        final BiomeLayout2DStage.Data chunkData = context.getChunkData(BiomeLayout2DStage.Data.class, sectionX, sectionZ);
        Biome[] biomes = new Biome[4*4];
        int i = 0;
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                biomes[i] = chunkData.biomes().get(i++).entrySet().stream().max((a, b) -> Float.compare(a.getValue(), b.getValue())).get().getKey();
            }
        }
        context.setChunkData(new Data(biomes), sectionX, sectionZ);
    }

    @Override
    public @NotNull Set<Class<? extends StageData>> getDependencies() {
        return Set.of(BiomeLayout2DStage.Data.class);
    }

    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public @NotNull Class<Data> getDataClass() {
        return Data.class;
    }

    public record Data(Biome[] biomes) implements StageData.Chunk {}
}
