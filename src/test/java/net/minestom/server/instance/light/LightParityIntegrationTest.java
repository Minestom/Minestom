package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class LightParityIntegrationTest {
    private static final int REGION_SIZE = 3;

    @Test
    public void test(Env env) throws URISyntaxException, IOException {
        Map<Vec, SectionEntry> sections = retrieveSections();
        // Generate our own light

        InstanceContainer instance = (InstanceContainer) env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        instance.setChunkLoader(new AnvilLoader(Path.of("./src/test/resources/net/minestom/server/instance/lighting")));

        List<CompletableFuture<Chunk>> futures = new ArrayList<>();

        int end = REGION_SIZE;
        // Load the chunks
        for (int x = 0; x < end; x++) {
            for (int z = 0; z < end; z++) {
                futures.add(instance.loadChunk(x, z));
            }
        }

        for (CompletableFuture<Chunk> future : futures) {
            future.join();
        }

        LightingChunk.relight(instance, instance.getChunks());

        int differences = 0;
        int differencesZero = 0;
        int blocks = 0;
        int sky = 0;

        for (Chunk chunk : instance.getChunks()) {
            if (chunk.getChunkX() == 0 || chunk.getChunkZ() == 0) {
                continue;
            }

            if (chunk.getChunkX() == end - 1 || chunk.getChunkZ() == end - 1) {
                continue;
            }

            for (int sectionIndex = chunk.getMinSection(); sectionIndex < chunk.getMaxSection(); sectionIndex++) {
                if (sectionIndex > 6) break;

                Section section = chunk.getSection(sectionIndex);

                Light sectionLight = section.blockLight();
                Light sectionSkyLight = section.skyLight();
                SectionEntry sectionEntry = sections.get(new Vec(chunk.getChunkX(), sectionIndex, chunk.getChunkZ()));
                if (sectionEntry == null) {
                    continue;
                }

                byte[] serverBlock = sectionLight.array();
                byte[] mcaBlock = sectionEntry.block;

                byte[] serverSky = sectionSkyLight.array();
                byte[] mcaSky = sectionEntry.sky;

                for (int x = 0; x < 16; ++x) {
                    for (int y = 0; y < 16; ++y) {
                        for (int z = 0; z < 16; ++z) {
                            int index = x | (z << 4) | (y << 8);

                            {
                                int serverBlockValue = LightCompute.getLight(serverBlock, index);
                                int mcaBlockValue = mcaBlock.length == 0 ? 0 : LightCompute.getLight(mcaBlock, index);

                                if (serverBlockValue != mcaBlockValue) {
                                    if (serverBlockValue == 0) differencesZero++;
                                    else differences++;
                                    blocks++;
                                }
                            }

                            // Mojang's sky lighting is wrong
                            {
                                int serverSkyValue = LightCompute.getLight(serverSky, index);
                                int mcaSkyValue = mcaSky.length == 0 ? 0 : LightCompute.getLight(mcaSky, index);

                                if (serverSkyValue != mcaSkyValue) {
                                    if (serverSkyValue == 0) differencesZero++;
                                    else differences++;
                                    sky++;
                                }
                            }
                        }
                    }
                }
            }
        }

        assertEquals(0, blocks);
        assertEquals(0, sky);
        assertEquals(0, differences);
        assertEquals(0, differencesZero);
    }

    record SectionEntry(Palette blocks, byte[] sky, byte[] block) {
    }

    private static Map<Vec, SectionEntry> retrieveSections() throws IOException, URISyntaxException {
        var worldDir = Files.createTempDirectory("minestom-light-parity-test");
        var mcaFile = worldDir.resolve("region").resolve("r.0.0.mca");
        Files.createDirectories(mcaFile.getParent());
        try (var is = LightParityIntegrationTest.class.getResourceAsStream("/net/minestom/server/instance/lighting/region/r.0.0.mca")) {
            Files.copy(Objects.requireNonNull(is), mcaFile);
        }

        var instance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD); // Never registered
        var anvilLoader = new AnvilLoader(worldDir);

        Map<Vec, SectionEntry> sections = new HashMap<>();
        // Read from anvil
        for (int x = 1; x < REGION_SIZE - 1; x++) {
            for (int z = 1; z < REGION_SIZE - 1; z++) {
                var chunk = anvilLoader.loadChunk(instance, x, z);
                if (chunk == null) continue;

                for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
                    var section = chunk.getSection(sectionY);
                    sections.put(new Vec(x, sectionY, z), new SectionEntry(section.blockPalette(), section.skyLight().array(), section.blockLight().array()));
                }
            }
        }
        return sections;
    }
}