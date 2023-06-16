package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jglrxavpok.hephaistos.mca.AnvilException;
import org.jglrxavpok.hephaistos.mca.BlockState;
import org.jglrxavpok.hephaistos.mca.ChunkSection;
import org.jglrxavpok.hephaistos.mca.RegionFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@EnvTest
public class LightParityIntegrationTest {

    @Test
    public void test(Env env) throws URISyntaxException, IOException, AnvilException {
        Map<Vec, SectionEntry> sections = retrieveSections();
        // Generate our own light

        InstanceContainer instance = (InstanceContainer) env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        instance.setChunkLoader(new AnvilLoader(Path.of("./src/test/resources/net/minestom/server/instance/lighting")));

        int end = 4;
        // Load the chunks
        for (int x = 0; x < end; x++) {
            for (int z = 0; z < end; z++) {
                instance.loadChunk(x, z).join();
            }
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
                if (sectionIndex != 3) continue;

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

        assertEquals(0, differences);
        assertEquals(0, differencesZero);
        assertEquals(0, blocks);
        assertEquals(0, sky);
    }

    record SectionEntry(Palette blocks, byte[] sky, byte[] block) {
    }

    private static Map<Vec, SectionEntry> retrieveSections() throws IOException, URISyntaxException, AnvilException {
        URL defaultImage = LightParityIntegrationTest.class.getResource("/net/minestom/server/instance/lighting/region/r.0.0.mca");
        assert defaultImage != null;
        File imageFile = new File(defaultImage.toURI());
        var regionFile = new RegionFile(new RandomAccessFile(imageFile, "rw"),
                0, 0, -64, 384);

        Map<Vec, SectionEntry> sections = new HashMap<>();
        // Read from anvil
        for (int x = 1; x < 3; x++) {
            for (int z = 1; z < 3; z++) {
                var chunk = regionFile.getChunk(x, z);
                if (chunk == null) continue;

                for (int yLevel = chunk.getMinY(); yLevel <= chunk.getMaxY(); yLevel += 16) {
                    var section = chunk.getSection((byte) (yLevel/16));
                    var palette = loadBlocks(section);
                    var sky = section.getSkyLights();
                    var block = section.getBlockLights();
                    sections.put(new Vec(x, section.getY(), z), new SectionEntry(palette, sky, block));
                }
            }
        }
        return sections;
    }

    private static Palette loadBlocks(ChunkSection section) throws AnvilException {
        var palette = Palette.blocks();
        for (int x = 0; x < Chunk.CHUNK_SECTION_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SECTION_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    final BlockState blockState = section.get(x, y, z);
                    final String blockName = blockState.getName();
                    Block block = Objects.requireNonNull(Block.fromNamespaceId(blockName))
                            .withProperties(blockState.getProperties());
                    palette.set(x, y, z, block.stateId());
                }
            }
        }
        return palette;
    }
}