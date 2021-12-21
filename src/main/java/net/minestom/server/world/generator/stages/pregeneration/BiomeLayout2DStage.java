package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.utils.noise.Noise2D;
import net.minestom.server.world.generator.GenerationContext;

public class BiomeLayout2DStage implements PreGenerationStage {
    private final Noise2D biomeTemperatureNoise;
    private final Noise2D biomeHumidityNoise;
    private final float biomeSize;

    public BiomeLayout2DStage(Noise2D biomeTemperatureNoise, Noise2D biomeHumidityNoise, float biomeSize) {
        this.biomeTemperatureNoise = biomeTemperatureNoise;
        this.biomeHumidityNoise = biomeHumidityNoise;
        this.biomeSize = biomeSize;
    }

    @Override
    public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
        // FIXME: 2021. 12. 19. POC! - Needs to be properly implemented, currently it decides between the first two
        //  biomes based on the temperature noise
    }

    @Override
    public int getRange() {
        return 10;
    }
}
