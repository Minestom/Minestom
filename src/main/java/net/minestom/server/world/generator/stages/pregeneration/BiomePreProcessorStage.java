package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.GenerationContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BiomePreProcessorStage implements PreGenerationStage<BiomePreProcessorStage.Data> {
    @Override
    public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
        final BiomeProviderStage.Data biomeData = context.getInstanceData(BiomeProviderStage.Data.class);
        final float minTemp = biomeData.biomes().stream().map(Biome::temperature).min(Float::compareTo).orElse(-1f);
        final float maxTemp = biomeData.biomes().stream().map(Biome::temperature).max(Float::compareTo).orElse(1f);
        final float minPrecipitation = biomeData.biomes().stream().map(Biome::downfall).min(Float::compareTo).orElse(-1f);
        final float maxPrecipitation = biomeData.biomes().stream().map(Biome::downfall).max(Float::compareTo).orElse(1f);
        final float slopeTemp = 2 / (maxTemp - minTemp);
        final float slopePrecipitation = 2 / (maxPrecipitation - minPrecipitation);
        final BiomeWithMappedTemperatureAndPrecipitationValues[] biomeWithMappedTemperatureAndPrecipitationValues = new BiomeWithMappedTemperatureAndPrecipitationValues[biomeData.biomes().size()];
        int i = 0;
        for (Biome biome : biomeData.biomes()) {
            biomeWithMappedTemperatureAndPrecipitationValues[i++] = new BiomeWithMappedTemperatureAndPrecipitationValues(biome,
                    -1 + slopeTemp * (biome.temperature() - minTemp),
                    -1 + slopePrecipitation * (biome.downfall() - minPrecipitation));
        }
        context.setInstanceData(new Data(biomeWithMappedTemperatureAndPrecipitationValues));
    }

    @Override
    public @NotNull Set<Class<? extends StageData>> getDependencies() {
        return Set.of(BiomeProviderStage.Data.class);
    }

    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public @NotNull Class<Data> getDataClass() {
        return Data.class;
    }

    public static class Data implements StageData.Instance {
        private final BiomeWithMappedTemperatureAndPrecipitationValues[] biomeWithMappedTemperatureAndPrecipitationValues;

        public Data(BiomeWithMappedTemperatureAndPrecipitationValues[] biomeWithMappedTemperatureAndPrecipitationValues) {
            this.biomeWithMappedTemperatureAndPrecipitationValues = biomeWithMappedTemperatureAndPrecipitationValues;
        }

        public Map<Biome, Float> getBiomesInfluence(float temperature, float precipitation, float blending) {
            //TODO Verify math
            final float targetMin = -blending;
            final float targetMax = blending;
            final float[] avgAbsDistances = new float[biomeWithMappedTemperatureAndPrecipitationValues.length];
            final HashMap<Biome, Float> result = new HashMap<>();
            for (int i = 0; i < biomeWithMappedTemperatureAndPrecipitationValues.length; i++) {
                final BiomeWithMappedTemperatureAndPrecipitationValues biome = biomeWithMappedTemperatureAndPrecipitationValues[i];
                avgAbsDistances[i] = Math.abs(((biome.temperature - temperature) + (biome.precipitation - precipitation)) / 2);
            }
            for (int i = 0; i < avgAbsDistances.length; i++) {
                if (MathUtils.isBetween(avgAbsDistances[i], targetMin, targetMax)) {
                    result.put(biomeWithMappedTemperatureAndPrecipitationValues[i].biome, avgAbsDistances[i]);
                }
            }
            if (result.isEmpty()) {
                int closestToZeroIndex = 0;
                float closestToZeroValue = 1000;
                for (int i = 0; i < avgAbsDistances.length; i++) {
                    if (avgAbsDistances[i] < closestToZeroValue) {
                        closestToZeroValue =avgAbsDistances[i];
                        closestToZeroIndex = i;
                    }
                }
                result.put(biomeWithMappedTemperatureAndPrecipitationValues[closestToZeroIndex].biome, 1f);
                return result;
            }
            final float min = result.values().stream().min(Float::compareTo).orElse(0f);
            final float max = result.values().stream().max(Float::compareTo).orElse(0f);
            final float slope = 1 / (max-min);
            result.replaceAll((biome, value) -> slope * (value-min));
            return result;
        }
    }
    private record BiomeWithMappedTemperatureAndPrecipitationValues(Biome biome, float temperature, float precipitation) {}
}
