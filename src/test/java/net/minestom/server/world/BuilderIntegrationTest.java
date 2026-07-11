package net.minestom.server.world;

import net.minestom.server.world.attribute.EnvironmentAttributeMap;
import net.minestom.server.world.biome.Biome;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnvTest
public class BuilderIntegrationTest {
    @Test
    public void testBiome(Env env) {
        Biome existing = env.process().biome().get(Biome.CHERRY_GROVE);
        assertNotNull(existing);
        Biome.Builder builder = Biome.builder(existing);
        assertEquals(existing, builder.build());
    }

    @Test
    public void testDimensionType(Env env) {
        DimensionType existing = env.process().dimensionType().get(DimensionType.THE_NETHER);
        assertNotNull(existing);
        DimensionType.Builder builder = DimensionType.builder(existing);
        assertEquals(existing, builder.build());
    }

    @Test
    public void testEnvironmentAttributeMap(Env env) {
        DimensionType existing = env.process().dimensionType().get(DimensionType.OVERWORLD);
        assertNotNull(existing);
        EnvironmentAttributeMap.Builder builder = EnvironmentAttributeMap.builder(existing.attributes());
        assertEquals(existing.attributes(), builder.build());
    }
}
