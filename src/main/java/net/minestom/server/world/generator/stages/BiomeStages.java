package net.minestom.server.world.generator.stages;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.block.SectionBlockCache;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.noise.Noise2D;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.BlockPool;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.stages.generation.GenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.PreGenerationStage;
import net.minestom.server.world.generator.stages.pregeneration.StageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Static factory methods (and implementations) for generating a weighted biome layout.<br>
 * Order of stages:
 * <ol>
 *     <li>{@link #provider(Set)} (which uses {@link #provider(Set, boolean)} with auto register set to {@code true}) -
 *     This stage is responsible for declaring all the possible biomes in the generated world</li>
 *     <li>{@link #biomeDistribution2D(Noise2D, Noise2D)} - Used to assign biomes to chunks</li>
 *     <li>{@link #biomeWeightGrid2D()} - Used to create the weighted biome grid which can be used to blend biome terrain</li>
 *     <li>{@link #biomeGrid2D()} - Decides the dominant biome</li>
 *     <li>{@link #fill2D()} - Uses previous data to fill columns with the same chunk</li>
 * </ol>
 */
public class BiomeStages {
    public static final ChunkBiomeGridStage CHUNK_BIOME_GRID_STAGE = new ChunkBiomeGridStage();
    private static final GenerationStage BIOME_FILL_2D = new BiomeFillStage2D();
    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeStages.class);

    ///////////////////////////////////////////////////////////////////////////
    // Static factory methods
    ///////////////////////////////////////////////////////////////////////////

    public static PreGenerationStage<BiomeData> provider(Set<DefaultBiomeSettings> biomes) {
        return provider(biomes, true);
    }

    public static PreGenerationStage<BiomeData> provider(Set<DefaultBiomeSettings> biomes, boolean autoRegister) {
        return new BiomeProviderStage(biomes, autoRegister);
    }

    public static PreGenerationStage<ChunkBiome> biomeDistribution2D(Noise2D temperatureNoise, Noise2D precipitationNoise) {
        //TODO Allow custom grid spacing
        return new BiomeDistributionStage(temperatureNoise, precipitationNoise, 1);
    }

    public static PreGenerationStage<ChunkBiomeWeightGrid> biomeWeightGrid2D() {
        return new ChunkBiomeWeightGridStage();
    }

    public static PreGenerationStage<ChunkBiomeGrid> biomeGrid2D() {
        return CHUNK_BIOME_GRID_STAGE;
    }

    public static GenerationStage fill2D() {
        return BIOME_FILL_2D;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implementations
    ///////////////////////////////////////////////////////////////////////////

    private static class BiomeProviderStage implements PreGenerationStage<BiomeData> {
        private final Set<DefaultBiomeSettings> biomes;
        final Map<DefaultBiomeSettings, Vec> biomesToWeather = new HashMap<>();

        public BiomeProviderStage(Set<DefaultBiomeSettings> biomes, boolean autoRegister) {
            this.biomes = Collections.unmodifiableSet(biomes);
            for (DefaultBiomeSettings biomeSettings : biomes) {
                if (!MinecraftServer.getBiomeManager().unmodifiableCollection().contains(biomeSettings.biome())) {
                    if (autoRegister) {
                        MinecraftServer.getBiomeManager().addBiome(biomeSettings.biome());
                    } else {
                        LOGGER.warn("Biome {} isn't registered in the BiomeManager, chunks containing this biome will be rejected by the client!", biomeSettings.biome().name());
                    }
                }
                biomesToWeather.put(biomeSettings, new Vec(biomeSettings.biome().temperature(), biomeSettings.biome().downfall(),0));
            }
        }

        @Override
        public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
            context.setInstanceData((BiomeData)new Data(biomes, biomesToWeather));
        }

        @Override
        public int getRange() {
            return 0;
        }

        @Override
        public @NotNull Class<BiomeData> getDataClass() {
            return BiomeData.class;
        }

        public record Data(Set<DefaultBiomeSettings> biomes, Map<DefaultBiomeSettings, Vec> biomesToWeather) implements BiomeData {
            public DefaultBiomeSettings getBiome(double temperature, double precipitation) {
                final Vec target = new Vec(temperature, precipitation, 0);
                var distances = biomesToWeather.entrySet()
                        .stream()
                        .map(x -> Map.entry(x.getKey(), x.getValue().distanceSquared(target)))
                        .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                        .toList();
                return distances.get(0).getKey();
            }
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class BiomeDistributionStage implements PreGenerationStage<ChunkBiome> {
        private final Noise2D temperature;
        private final Noise2D precipitation;
        private final int gridSpacing;

        private BiomeDistributionStage(Noise2D temperature, Noise2D precipitation, int gridSpacing) {
            this.temperature = temperature;
            this.precipitation = precipitation;
            this.gridSpacing = gridSpacing;
        }

        @Override
        public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
            if (sectionX % gridSpacing == 0 || sectionZ % gridSpacing == 0) {
                final BiomeData biomeData = context.getInstanceData(BiomeData.class);
                final double globalX = (sectionX + .5) * Chunk.CHUNK_SIZE_X;
                final double globalZ = (sectionZ + .5) * Chunk.CHUNK_SIZE_Z;
                final DefaultBiomeSettings biome = biomeData.getBiome(temperature.getValue(globalX, globalZ), precipitation.getValue(globalX, globalZ));
                context.setChunkData(new Data(biome), sectionX, sectionZ);
            } else {
                context.setChunkData(new Data(null), sectionX, sectionZ);
            }
        }

        @Override
        public int getRange() {
            return 1;
        }

        @Override
        public @NotNull
        Set<Class<? extends StageData>> getDependencies() {
            return Set.of(BiomeData.class);
        }

        @Override
        public @NotNull
        Class<ChunkBiome> getDataClass() {
            return ChunkBiome.class;
        }

        private record Data(DefaultBiomeSettings biome) implements ChunkBiome {
        }
    }

    private static class ChunkBiomeWeightGridStage implements PreGenerationStage<ChunkBiomeWeightGrid> {
        private static final int GRID_SIZE = 4;
        private static final double[][] WEIGHTS = new double[MathUtils.square(GRID_SIZE)][];

        static {
            // Centers of chunks 3x3
            Vec[] biomePoints = new Vec[9];
            // Centers of biome cells 4x4 in 0;0
            Vec[] targetPoints = new Vec[MathUtils.square(GRID_SIZE)];
            {
                int i = 0;
                for (int x = -Chunk.CHUNK_SIZE_X / 2; x < Chunk.CHUNK_SIZE_X * 2; x += Chunk.CHUNK_SIZE_X) {
                    for (int z = -Chunk.CHUNK_SIZE_Z / 2; z < Chunk.CHUNK_SIZE_Z * 2; z += Chunk.CHUNK_SIZE_Z) {
                        biomePoints[i++] = new Vec(x, z);
                    }
                }
                i = 0;
                for (int x = GRID_SIZE /2; x < Chunk.CHUNK_SIZE_X; x+= GRID_SIZE) {
                    for (int z = GRID_SIZE /2; z < Chunk.CHUNK_SIZE_Z; z+= GRID_SIZE) {
                        targetPoints[i++] = new Vec(x, z);
                    }
                }
            }
            final double max = biomePoints[0].distanceSquared(targetPoints[targetPoints.length-1]);
            for (int i = 0; i < targetPoints.length; i++) {
                final double[] weight = new double[biomePoints.length];
                double sum = 0;
                for (int j = 0; j < biomePoints.length; j++) {
                    weight[j] = biomePoints[j].distanceSquared(targetPoints[i]);
                    sum += (max - weight[j]);
                }
                for (int k = 0; k < weight.length; k++) {
                    weight[k] =  weight[k] / sum;
                }
                WEIGHTS[i] = weight;
            }
        }

        @Override
        public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
            DefaultBiomeSettings[] biomeSettings = new DefaultBiomeSettings[] {
              context.getChunkData(ChunkBiome.class, sectionX-1, sectionZ-1).biome(),
              context.getChunkData(ChunkBiome.class, sectionX-1, sectionZ).biome(),
              context.getChunkData(ChunkBiome.class, sectionX-1, sectionZ+1).biome(),
              context.getChunkData(ChunkBiome.class, sectionX, sectionZ-1).biome(),
              context.getChunkData(ChunkBiome.class, sectionX, sectionZ).biome(),
              context.getChunkData(ChunkBiome.class, sectionX, sectionZ+1).biome(),
              context.getChunkData(ChunkBiome.class, sectionX+1, sectionZ-1).biome(),
              context.getChunkData(ChunkBiome.class, sectionX+1, sectionZ).biome(),
              context.getChunkData(ChunkBiome.class, sectionX+1, sectionZ+1).biome()
            };
            final Map<DefaultBiomeSettings, List<Integer>> biomeWeightIndexes = new HashMap<>();
            for (int i = 0; i < biomeSettings.length; i++) {
                biomeWeightIndexes.computeIfAbsent(biomeSettings[i], k -> new ArrayList<>()).add(i);
            }
            final List<Map<DefaultBiomeSettings, Double>> maps = new ArrayList<>();
            if (biomeWeightIndexes.size() == 1) {
                final HashMap<DefaultBiomeSettings, Double> map = new HashMap<>();
                map.put(biomeSettings[0], 1d);
                for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
                    maps.add(map);
                }
            } else {
                int i = 0;
                for (int x = 0; x < GRID_SIZE; x++) {
                    for (int z = 0; z < GRID_SIZE; z++) {
                        final HashMap<DefaultBiomeSettings, Double> map = new HashMap<>();
                        final double[] w = WEIGHTS[i++];
                        biomeWeightIndexes.entrySet().parallelStream().forEach(e -> {
                            double sum = 0;
                            for (Integer index : e.getValue()) {
                                sum += w[index];
                            }
                            map.put(e.getKey(), sum);
                        });
                        maps.add(map);
                    }
                }
            }
            context.setChunkData(new Data(maps), sectionX, sectionZ);
        }

        @Override
        public int getRange() {
            return 0;
        }

        @Override
        public @NotNull Set<Class<? extends StageData>> getDependencies() {
            return Set.of(ChunkBiome.class);
        }

        @Override
        public @NotNull Class<ChunkBiomeWeightGrid> getDataClass() {
            return ChunkBiomeWeightGrid.class;
        }

        private record Data(List<Map<DefaultBiomeSettings, Double>> biomes) implements ChunkBiomeWeightGrid {}
    }

    private static class ChunkBiomeGridStage implements PreGenerationStage<ChunkBiomeGrid> {

        @Override
        public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
            final List<Map<DefaultBiomeSettings, Double>> biomes = context.getChunkData(ChunkBiomeWeightGrid.class, sectionX, sectionZ).biomes();
            DefaultBiomeSettings[] biomeSettings = new BiomeSettings[4*4];
            int i = 0;
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    biomeSettings[i] = biomes.get(i++).entrySet().stream().sorted((a,b) -> Double.compare(b.getValue(),a.getValue())).findFirst().get().getKey();
                }
            }
            context.setChunkData(new Data(biomeSettings), sectionX, sectionZ);
        }

        @Override
        public int getRange() {
            return 0;
        }

        @Override
        public @NotNull Set<Class<? extends StageData>> getDependencies() {
            return Set.of(ChunkBiomeWeightGrid.class);
        }

        @Override
        public @NotNull Class<ChunkBiomeGrid> getDataClass() {
            return ChunkBiomeGrid.class;
        }

        private record Data(DefaultBiomeSettings[] biomes) implements ChunkBiomeGrid {}
    }

    private static class BiomeFillStage2D implements GenerationStage {
        @Override
        public void process(GenerationContext context, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ) {
            final ChunkBiomeGrid chunkData = context.getChunkData(ChunkBiomeGrid.class, sectionX, sectionZ);
            int i = 0;
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    final int id = chunkData.biomes()[i++].biome().id();
                    for (int y = 0; y < 4; y++) {
                        biomePalette.set(x,y,z, id);
                    }
                }
            }
        }

        @Override
        public @NotNull Set<Class<? extends StageData>> getDependencies() {
            return Set.of(ChunkBiomeGrid.class);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Interfaces for data
    ///////////////////////////////////////////////////////////////////////////

    public interface BiomeData extends StageData.Instance {
        Set<DefaultBiomeSettings> biomes();
        DefaultBiomeSettings getBiome(double temperature, double precipitation);
    }

    public interface ChunkBiome extends StageData.Chunk {
        @Nullable DefaultBiomeSettings biome();
    }

    public interface ChunkBiomeWeightGrid extends StageData.Chunk {
        List<Map<DefaultBiomeSettings, Double>> biomes();
    }

    public interface ChunkBiomeGrid extends StageData.Chunk {
        DefaultBiomeSettings[] biomes();
    }

    public interface DefaultBiomeSettings {
        Biome biome();
        BlockPool terrainBlocks();
        Noise2D heightNoise();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Biome settings
    ///////////////////////////////////////////////////////////////////////////

    private record BiomeSettings(Biome biome, BlockPool terrainBlocks, Noise2D heightNoise) implements DefaultBiomeSettings {}

    public static class BiomeSettingsBuilder {
        private static final BlockPool DEFAULT_POOL = new BlockPool(((x, y, z) -> 0)) {{
            addBlock(Block.STONE, 1, new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE));
        }};
        private static final Noise2D DEFAULT_NOISE = ((x, y) -> 0);
        private final Biome biome;
        private BlockPool terrainBlocks;
        private Noise2D heightNoise;

        private BiomeSettingsBuilder(@NotNull Biome biome) {
            this.biome = biome;
            this.terrainBlocks = DEFAULT_POOL;
            this.heightNoise = DEFAULT_NOISE;
        }

        public static BiomeSettingsBuilder newBuilder(Biome biome) {
            return new BiomeSettingsBuilder(biome);
        }

        public BiomeSettingsBuilder setTerrainBlocks(BlockPool terrainBlocks) {
            this.terrainBlocks = terrainBlocks;
            return this;
        }

        public BiomeSettingsBuilder setHeightNoise(Noise2D heightNoise) {
            this.heightNoise = heightNoise;
            return this;
        }

        public BiomeSettings build() {
            return new BiomeSettings(biome, terrainBlocks, heightNoise);
        }
    }
}
