package net.minestom.server.world.generator.stages;

import net.minestom.server.tag.Tag;
import net.minestom.server.utils.noise.Noise2D;
import net.minestom.server.world.generator.GenerationContext;
import net.minestom.server.world.generator.WorldGenerationStage;

public class BiomeLayout2DStage implements WorldGenerationStage {
    public static final Tag<int[]> BIOME_DATA = Tag.IntArray("BiomeData");
    private final Noise2D biomeTemperatureNoise;
    private final Noise2D biomeHumidityNoise;
    private final float biomeSize;

    public BiomeLayout2DStage(Noise2D biomeTemperatureNoise, Noise2D biomeHumidityNoise, float biomeSize) {
        this.biomeTemperatureNoise = biomeTemperatureNoise;
        this.biomeHumidityNoise = biomeHumidityNoise;
        this.biomeSize = biomeSize;
    }

    @Override
    public void lookAround(GenerationContext context, int chunkX, int chunkZ) {
        // FIXME: 2021. 12. 19. POC! - Needs to be properly implemented, currently it decides between the first two
        //  biomes based on the temperature noise
    }

    @Override
    public int getLookAroundRange() {
        return 10;
    }
}
