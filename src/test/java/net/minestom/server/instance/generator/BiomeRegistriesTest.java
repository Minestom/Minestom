package net.minestom.server.instance.generator;

import net.minestom.server.instance.generator.GeneratorImpl.GenSection;
import net.minestom.server.registry.Registries;
import net.minestom.server.world.biome.Biome;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RegistriesTest
public class BiomeRegistriesTest {

    private static int plainsId, badlandsId;

    @BeforeAll
    public static void prepareTest(Registries registries) {
        plainsId = registries.biome().getId(Biome.PLAINS);
        badlandsId = registries.biome().getId(Biome.BADLANDS);
    }

    @Test
    public void chunkBiomeSet(Registries registries) {
        final int minSection = -1;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, _ -> new GenSection(registries.biome()));
        var chunkUnits = GeneratorImpl.chunk(registries.biome(), sections, chunkX, minSection, chunkZ);
        Generator generator = unit -> {
            var modifier = unit.modifier();
            modifier.setBiome(48, -16, -32, Biome.BADLANDS);
            modifier.setBiome(48 + 8, 0, -32, Biome.BADLANDS);
        };
        generator.generate(chunkUnits);

        // Reminder because I (matt) forgot: biome palettes are 4x4x4 sections, so x=2 is really x=8 in the chunk.
        assertEquals(badlandsId, sections[0].biomes().get(0, 0, 0));
        assertEquals(plainsId, sections[1].biomes().get(1, 0, 0));
        assertEquals(badlandsId, sections[1].biomes().get(2, 0, 0));
    }

    @Test
    public void chunkBiomeFill(Registries registries) {
        final int minSection = -1;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, _ -> new GenSection(registries.biome()));
        var chunkUnits = GeneratorImpl.chunk(registries.biome(), sections, chunkX, minSection, chunkZ);
        Generator generator = chunk -> {
            var modifier = chunk.modifier();
            modifier.fillBiome(Biome.PLAINS);
        };
        generator.generate(chunkUnits);
        for (var section : sections) {
            section.biomes().getAll((_, _, _, value) ->
                    assertEquals(plainsId, value));
        }
    }

}
