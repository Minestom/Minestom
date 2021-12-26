package net.minestom.server.world.generator.stages.pregeneration;

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
        final BiomeWithTemperatureAndPrecipitationValues[] biomeWithTemperatureAndPrecipitationValues = new BiomeWithTemperatureAndPrecipitationValues[biomeData.biomes().size()];
        int i = 0;
        for (Biome biome : biomeData.biomes()) {
            biomeWithTemperatureAndPrecipitationValues[i++] = new BiomeWithTemperatureAndPrecipitationValues(biome,
                    -1 + slopeTemp * (biome.temperature() - minTemp),
                    -1 + slopePrecipitation * (biome.downfall() - minPrecipitation));
        }
        context.setInstanceData(new Data(biomeWithTemperatureAndPrecipitationValues));
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
        private final BiomeWithTemperatureAndPrecipitationValues[] biomeWithTemperatureAndPrecipitationValues;

        public Data(BiomeWithTemperatureAndPrecipitationValues[] biomeWithTemperatureAndPrecipitationValues) {
            this.biomeWithTemperatureAndPrecipitationValues = biomeWithTemperatureAndPrecipitationValues;
        }

        public Map<Biome, Float> getBiomesInfluence(float temperature, float precipitation, float blending) {
            List<BiomeWithTemperatureAndPrecipitationValues> list = new ArrayList<>();
            for (BiomeWithTemperatureAndPrecipitationValues b : biomeWithTemperatureAndPrecipitationValues) {
                list.add(new BiomeWithTemperatureAndPrecipitationValues(b.biome, Math.abs(b.temperature - temperature), Math.abs(b.precipitation - precipitation)));
            }
            list.sort((a, b) -> Float.compare(Math.abs(a.temperature-a.precipitation), Math.abs(b.temperature-b.precipitation)));
            final List<BiomeWithTemperatureAndPrecipitationValues> results = list.stream().filter(x -> MathUtils.isBetween(x.temperature, 0, blending) && MathUtils.isBetween(x.precipitation, 0, blending)).toList();
            final HashMap<Biome, Float> map = new HashMap<>();
            if (results.isEmpty()) {
                final BiomeWithTemperatureAndPrecipitationValues closestMatch = list.stream().findFirst().get();
                map.put(closestMatch.biome, 1f);
            } else {
                final Set<Float> distanceDistances = results.stream().map(a -> Math.abs(a.temperature - a.precipitation)).collect(Collectors.toSet());
                final Float min = distanceDistances.stream().min(Float::compareTo).get();
                final Float max = distanceDistances.stream().max(Float::compareTo).get();
                final float slope = 1 / (max-min);
                for (BiomeWithTemperatureAndPrecipitationValues result : results) {
                    map.put(result.biome, slope * Math.abs(result.temperature - result.precipitation));
                }
            }
            return map;
        }
    }
    private record BiomeWithTemperatureAndPrecipitationValues(Biome biome, float temperature, float precipitation) {}
}
