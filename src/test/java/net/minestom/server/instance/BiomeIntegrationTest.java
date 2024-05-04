package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class BiomeIntegrationTest {
    private static Biome PLAINS;
    private static int PLAINS_ID;

    @BeforeAll
    public static void prepareTest(Env env) {
        PLAINS = MinecraftServer.getBiomeManager().getByName(NamespaceID.from("minecraft:plains"));
        PLAINS_ID = MinecraftServer.getBiomeManager().getId(PLAINS);
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
        Generator generator = chunk -> {
            var modifier = chunk.modifier();
            modifier.setBiome(48, 0, -32, PLAINS);
            modifier.setBiome(48 + 8, 0, -32, PLAINS);
        };
        generator.generate(chunkUnits);

        assertEquals(PLAINS_ID, sections[0].biomePalette().get(0, 0, 0));
        assertEquals(0, sections[0].biomePalette().get(1, 0, 0));
        assertEquals(PLAINS_ID, sections[0].biomePalette().get(2, 0, 0));
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
            modifier.fillBiome(PLAINS);
        };
        generator.generate(chunkUnits);
        for (var section : sections) {
            section.biomePalette().getAll((x, y, z, value) ->
                    assertEquals(PLAINS_ID, value));
        }
    }

}
