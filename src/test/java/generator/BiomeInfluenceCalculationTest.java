package generator;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Debug;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.stages.pregeneration.BiomePreProcessorStage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BiomeInfluenceCalculationTest {
    @Test
    public void temperatureTestWithSamePrecipitation() {
        final Biome temp1 = Biome.builder().temperature(1f).build();
        final Biome temp0 = Biome.builder().temperature(0f).build();
        final BiomePreProcessorStage.Data data = new BiomePreProcessorStage.Data(Map.of(temp1, new Vec(1, 0, 0), temp0, new Vec(-1, 0, 0)));
        final Map<Biome, Float> test0Result = data.getBiomesInfluence(1f, 0, 1);
        assertEquals(1, test0Result.size());
        assertTrue(test0Result.containsKey(temp1));
        final Map<Biome, Float> test1Result = data.getBiomesInfluence(-.1f, 0, 1);
        assertEquals(1, test1Result.size());
        assertTrue(test1Result.containsKey(temp0));
        final Map<Biome, Float> test2Result = data.getBiomesInfluence(0f, 0, 0);
        assertEquals(2, test2Result.size());
        assertTrue(test2Result.containsKey(temp0));
        assertTrue(test2Result.containsKey(temp1));
    }
}
