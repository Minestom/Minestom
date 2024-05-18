package net.minestom.server.instance;

import net.minestom.server.instance.generator.Generator;
import net.minestom.server.world.biome.Biome;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class BiomeIntegrationTest {

    private static int PLAINS_ID, BADLANDS_ID;

    @BeforeAll
    public static void prepareTest(Env env) {
        PLAINS_ID = env.process().biome().getId(Biome.PLAINS);
        BADLANDS_ID = env.process().biome().getId(Biome.BADLANDS);
    }

    @Test
    public void chunkBiomeSet(Env env) {
        final int minSection = -1;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        Section[] sections = new Section[sectionCount];
        Arrays.setAll(sections, i -> new Section());
        var chunkUnits = GeneratorImpl.chunk(minSection, maxSection, List.of(sections), chunkX, chunkZ);
        Generator generator = unit -> {
            var modifier = unit.modifier();
            modifier.setBiome(48, -16, -32, Biome.BADLANDS);
            modifier.setBiome(48 + 8, 0, -32, Biome.BADLANDS);
        };
        generator.generate(chunkUnits);

        // Reminder because I (matt) forgot: biome palettes are 4x4x4 sections, so x=2 is really x=8 in the chunk.
        assertEquals(BADLANDS_ID, sections[0].biomePalette().get(0, 0, 0));
        assertEquals(PLAINS_ID, sections[1].biomePalette().get(1, 0, 0));
        assertEquals(BADLANDS_ID, sections[1].biomePalette().get(2, 0, 0));
    }

    @Test
    public void chunkBiomeFill(Env env) {
        final int minSection = -1;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        Section[] sections = new Section[sectionCount];
        Arrays.setAll(sections, i -> new Section());
        var chunkUnits = GeneratorImpl.chunk(minSection, maxSection, List.of(sections), chunkX, chunkZ);
        Generator generator = chunk -> {
            var modifier = chunk.modifier();
            modifier.fillBiome(Biome.PLAINS);
        };
        generator.generate(chunkUnits);
        for (var section : sections) {
            section.biomePalette().getAll((x, y, z, value) ->
                    assertEquals(PLAINS_ID, value));
        }
    }

}
