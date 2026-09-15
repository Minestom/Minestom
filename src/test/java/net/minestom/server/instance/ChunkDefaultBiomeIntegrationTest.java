package net.minestom.server.instance;

import net.minestom.server.world.biome.Biome;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class ChunkDefaultBiomeIntegrationTest {

    @Test
    public void newChunkDefaultsToPlains(Env env) {
        var instance = env.createEmptyInstance();
        var chunk = instance.loadChunk(0, 0).join();

        assertEquals(Biome.PLAINS, instance.getBiome(0, 0, 0));
        assertEquals(Biome.PLAINS, instance.getBiome(15, chunk.getMaxSection() * 16 - 1, 15));
    }

    @Test
    public void resetRestoresPlains(Env env) {
        var instance = env.createEmptyInstance();
        var chunk = instance.loadChunk(0, 0).join();

        instance.setBiome(0, 0, 0, Biome.BADLANDS);
        assertEquals(Biome.BADLANDS, instance.getBiome(0, 0, 0));

        chunk.lockWriteLock();
        try {
            chunk.reset();
        } finally {
            chunk.unlockWriteLock();
        }
        assertEquals(Biome.PLAINS, instance.getBiome(0, 0, 0));
    }
}
