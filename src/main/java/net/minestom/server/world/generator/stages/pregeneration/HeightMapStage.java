package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.noise.Noise2D;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.GenerationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HeightMapStage implements PreGenerationStage<HeightMapStage.Data> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeightMapStage.class);
    private final Map<Biome, Noise2D> biomeHeightNoises;
    private final Noise2D defaultNoise;

    public HeightMapStage(Map<Biome, Noise2D> biomeHeightNoises, @Nullable Noise2D defaultNoise) {
        this.biomeHeightNoises = biomeHeightNoises;
        if (defaultNoise == null) {
            LOGGER.warn("No default noise was provided, biomes with undefined noise will have a constant height of 0!");
            this.defaultNoise = ((x, y) -> 0);
        } else {
            this.defaultNoise = defaultNoise;
        }
    }

    @Override
    public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
        final BiomeLayout2DStage.Data chunkData = context.getChunkData(BiomeLayout2DStage.Data.class, sectionX, sectionZ);
        int[] height = new int[Chunk.CHUNK_SIZE_X*Chunk.CHUNK_SIZE_Z];
        int i = 0;
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                int globalX = sectionX * Chunk.CHUNK_SIZE_X + x;
                int globalZ = sectionZ * Chunk.CHUNK_SIZE_Z + z;
                final Set<Map.Entry<Noise2D, Float>> noiseWeightEntrySet = chunkData.biomes()
                        .get(i++ / 4)
                        .entrySet()
                        .stream()
                        .map(e -> Map.entry(biomeHeightNoises.getOrDefault(e.getKey(), defaultNoise), e.getValue()))
                        .collect(Collectors.toSet());
                final double sum = noiseWeightEntrySet.stream().map(e -> e.getKey().getValue(globalX, globalZ)).reduce(Double::sum).orElse(0d);
                final float sumWeight = noiseWeightEntrySet.stream().map(Map.Entry::getValue).reduce(Float::sum).orElse(1f);
                height[i-1] = (int) (sum/sumWeight);
            }
        }
        context.setChunkData(new Data(height), sectionX, sectionZ);
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


    public record Data(int[] heightMap) implements StageData.Chunk {}
}
