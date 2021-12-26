package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.utils.noise.Noise2D;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.GenerationContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BiomeLayout2DStage implements PreGenerationStage<BiomeLayout2DStage.Data> {
    private static final int BIOME_GRID_RESOLUTION = 4;
    private final Noise2D precipitationNoise;
    private final Noise2D temperatureNoise;
    private final int range;
    private final float blending;

    public BiomeLayout2DStage(Noise2D precipitationNoise, Noise2D temperatureNoise, float blending, int range) {
        this.precipitationNoise = precipitationNoise;
        this.temperatureNoise = temperatureNoise;
        this.range = range;
        this.blending = blending;
    }

    @Override
    public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
        final BiomePreProcessorStage.Data biomes = context.getInstanceData(BiomePreProcessorStage.Data.class);
        final List<Map<Biome, Float>> data = new ArrayList<>(BIOME_GRID_RESOLUTION*BIOME_GRID_RESOLUTION);
        final int xEnd = sectionX * BIOME_GRID_RESOLUTION + BIOME_GRID_RESOLUTION;
        final int zEnd = sectionZ * BIOME_GRID_RESOLUTION + BIOME_GRID_RESOLUTION;
        for (int x = sectionX * BIOME_GRID_RESOLUTION; x < xEnd; x++) {
            for (int z = sectionZ * BIOME_GRID_RESOLUTION; z < zEnd; z++) {
                data.add(biomes.getBiomesInfluence((float) temperatureNoise.getValue(x, z), (float) precipitationNoise.getValue(x, z), blending));
            }
        }
        context.setChunkData(new Data(data), sectionX, sectionZ);
    }

    @Override
    public int getRange() {
        return range;
    }

    @Override
    public @NotNull Set<Class<? extends StageData>> getDependencies() {
        return Set.of(BiomePreProcessorStage.Data.class);
    }

    @Override
    public @NotNull Class<Data> getDataClass() {
        return Data.class;
    }

    public record Data(List<Map<Biome, Float>> biomes) implements StageData.Chunk {}
}
