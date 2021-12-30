package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.instance.Chunk;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.BiomeStages;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class HeightMapStage implements PreGenerationStage<HeightMapStage.HeightMapData> {

    @Override
    public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
        final BiomeStages.ChunkBiomeWeightGrid data = context.getChunkData(BiomeStages.ChunkBiomeWeightGrid.class, sectionX, sectionZ);
        final int[] ints = new int[16 * 16];
        int i = 0;
        for (int x = 0; x < 16; x++) {
            int b = (x / 4) * 4;
            double globalX = sectionX * Chunk.CHUNK_SIZE_X + x;
            for (int z = 0; z < 16; z++) {
                final Map<BiomeStages.DefaultBiomeSettings, Double> map = data.biomes().get(b + (z / 4));
                double globalZ = sectionZ * Chunk.CHUNK_SIZE_Z + z;
                ints[i++] = map.entrySet().stream().map(e -> e.getKey().heightNoise().getValue(globalX, globalZ)*e.getValue()).reduce(Double::sum).orElse(0d).intValue();
            }
        }
        context.setChunkData(new Data(ints), sectionX, sectionZ);
    }

    @Override
    public @NotNull Set<Class<? extends StageData>> getDependencies() {
        return Set.of(BiomeStages.ChunkBiomeWeightGrid.class);
    }

    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public @NotNull Class<HeightMapData> getDataClass() {
        return HeightMapData.class;
    }

    public interface HeightMapData extends StageData.Chunk {
        int[] heightMap();
    }

    private record Data(int[] heightMap) implements HeightMapData {}
}
