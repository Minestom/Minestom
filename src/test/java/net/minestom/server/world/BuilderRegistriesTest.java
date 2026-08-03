package net.minestom.server.world;

import net.minestom.server.registry.Registries;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;
import net.minestom.server.world.biome.Biome;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RegistriesTest
public class BuilderRegistriesTest {
    @Test
    public void testBiome(Registries registries) {
        Biome existing = registries.biome().get(Biome.CHERRY_GROVE);
        assertNotNull(existing);
        Biome.Builder builder = Biome.builder(existing);
        assertEquals(existing, builder.build());
    }

    @Test
    public void testDimensionType(Registries registries) {
        DimensionType existing = registries.dimensionType().get(DimensionType.THE_NETHER);
        assertNotNull(existing);
        DimensionType.Builder builder = DimensionType.builder(existing);
        assertEquals(existing, builder.build());
    }

    @Test
    public void testEnvironmentAttributeMap(Registries registries) {
        DimensionType existing = registries.dimensionType().get(DimensionType.OVERWORLD);
        assertNotNull(existing);
        EnvironmentAttributeMap.Builder builder = EnvironmentAttributeMap.builder(existing.attributes());
        assertEquals(existing.attributes(), builder.build());
    }
}
