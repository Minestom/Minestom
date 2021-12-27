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
         * @param blending range 0.0-8.0
         * @return
         */
        public @NotNull Map<Biome, Float> getBiomesInfluence(float temperature, float precipitation, float blending) {
            final Vec target = new Vec(temperature, precipitation, 0);
            final Set<Map.Entry<Biome, Float>> biomesDistance = biomes.entrySet().stream().map(x -> Map.entry(x.getKey(), (float)x.getValue().distanceSquared(target))).collect(Collectors.toSet());
            final Map<Biome, Float> result = biomesDistance.stream().filter(x -> x.getValue() < blending).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (!result.isEmpty()) {
                final float min = result.values().stream().min(Float::compareTo).get();
                final float max = result.values().stream().max(Float::compareTo).get();
                final float slope = 1 / (max - min);
                result.replaceAll((key, value) -> slope * value);
                return result;
            } else {
                final Optional<Biome> biome = biomesDistance.stream().min((a, b) -> Float.compare(a.getValue(), b.getValue())).map(Map.Entry::getKey);
                if (biome.isPresent()) {
                    final HashMap<Biome, Float> map = new HashMap<>();
                    map.put(biome.get(), 1f);
                    return map;
                } else {
                    return new HashMap<>();
                }
            }
        }
    }
}
