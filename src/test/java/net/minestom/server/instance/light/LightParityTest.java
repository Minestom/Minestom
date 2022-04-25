package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LightParityTest {

    @Test
    public void test() throws URISyntaxException, IOException, AnvilException {
        Map<Vec, SectionEntry> sections = retrieveSections();
        // Generate our own light
        Map<Vec, BlockLightCompute.Result> results = new HashMap<>();
        for (var entry : sections.entrySet()) {
            var vec = entry.getKey();
            var palette = entry.getValue().blocks;
            results.put(vec, BlockLightCompute.compute(palette));
        }
    }

    record SectionEntry(Palette blocks, byte[] sky, byte[] block) {
    }

    private static Map<Vec, SectionEntry> retrieveSections() throws IOException, URISyntaxException, AnvilException {
        URL defaultImage = LightParityTest.class.getResource("/region.mca");
        assert defaultImage != null;
        File imageFile = new File(defaultImage.toURI());
        var regionFile = new RegionFile(new RandomAccessFile(imageFile, "rw"),
                0, 0, -64, 384);

        Map<Vec, SectionEntry> sections = new HashMap<>();
        // Read from anvil
        // TODO: read all 32x32 chunks
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                var chunk = regionFile.getChunk(x, z);
                if (chunk == null) continue;
                for (var section : chunk.getSections().values()) {
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
