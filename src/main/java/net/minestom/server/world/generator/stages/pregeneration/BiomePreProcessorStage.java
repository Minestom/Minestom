package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.GenerationContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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
        final Map<Biome, Vec> biomes = new HashMap<>();
        for (Biome biome : biomeData.biomes()) {
            biomes.put(biome, new Vec(
                    -1 + slopeTemp * (biome.temperature() - minTemp),
                    -1 + slopePrecipitation * (biome.downfall() - minPrecipitation),
                    0));
        }
        context.setInstanceData(new Data(biomes));
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
        private final Map<Biome, Vec> biomes;

        public Data(Map<Biome, Vec> biomes) {
            this.biomes = biomes;
        }


        /**
         *
         * @param temperature range -1.0 - 1.0
         * @param precipitation range -1.0 - 1.0
         * @param threshold range 0.0 - 1.0, defines the minimum influence of a biome in order to be included in the result
         * @return
         */
        public @NotNull Map<Biome, Float> getBiomesInfluence(float temperature, float precipitation, float threshold) {
            if (biomes.size() == 0) return new HashMap<>();
            final Vec target = new Vec(temperature, precipitation, 0);
            final Map<Biome, Float> distances = biomes.entrySet().stream().map(x -> Map.entry(x.getKey(), (float)x.getValue().distanceSquared(target))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // Find min max
            float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
            for (Map.Entry<Biome, Float> entry : distances.entrySet()) {
                if (entry.getValue() > max) {max = entry.getValue();continue;}
                if (entry.getValue() < min) min = entry.getValue();
            }
            final float slope = 1 / (max - min);

            if (slope == Float.POSITIVE_INFINITY) {
                distances.replaceAll((k, v) -> 1f);
                return distances;
            }

            final HashMap<Biome, Float> result = new HashMap<>();
            if (threshold == 1f) {
                for (Map.Entry<Biome, Float> entry : distances.entrySet()) {
                    if (entry.getValue() == min) {
                        result.put(entry.getKey(), 1f);
                        break;
                    }
                }
            } else {
                for (Map.Entry<Biome, Float> entry : distances.entrySet()) {
                    float influence = 1 - slope * entry.getValue();
                    if (influence >= threshold) {
                        result.put(entry.getKey(), influence);
                    }
                }
            }
            return result;
        }
    }
}
