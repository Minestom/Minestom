package net.minestom.server.instance;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class ChunkBiomePaletteIntegrationTest {

    @Test
    public void directChunkBiomePaletteMatchesRegistrySizedSerializer(Env env) {
        Instance instance = env.createFlatInstance();
        Chunk chunk = instance.loadChunk(0, 0).join();
        final Palette biomes = chunk.getSection(chunk.getMinSection()).biomePalette();
        // more distinct values than an indirect biome palette can hold, forcing direct storage
        for (int i = 0; i < 16; i++) {
            biomes.set(i % 4, i / 4, 0, i);
        }

        final var serializer = Palette.biomeSerializer(env.process().biome().size());
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(serializer, biomes);
        final Palette deserialized = buffer.read(serializer);
        assertTrue(biomes.compare(deserialized));
    }
}
