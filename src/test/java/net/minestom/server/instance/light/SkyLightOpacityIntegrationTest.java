package net.minestom.server.instance.light;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class SkyLightOpacityIntegrationTest {

    @Test
    public void carpetDoesNotStopSky(Env env) {
        var instance = roofedInstance(env, Block.WHITE_CARPET);
        assertEquals(15, skyLight(instance, 8, 44, 8));
    }

    @Test
    public void tintedGlassStopsSky(Env env) {
        var instance = roofedInstance(env, Block.TINTED_GLASS);
        assertEquals(0, skyLight(instance, 8, 45, 8));
        assertEquals(14, skyLight(instance, 8, 44, 8));
    }

    @Test
    public void leavesDiffuseSky(Env env) {
        var instance = roofedInstance(env, Block.MANGROVE_LEAVES);
        assertEquals(14, skyLight(instance, 8, 44, 8));
    }

    private static Instance roofedInstance(Env env, Block block) {
        var instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        instance.setBlock(8, 45, 8, block);
        LightingChunk.relight(instance, instance.getChunks());
        return instance;
    }

    private static int skyLight(Instance instance, int x, int y, int z) {
        var chunk = Objects.requireNonNull(instance.getChunkAt(x, z));
        return chunk.getSectionAt(y).skyLight().getLevel(x & 15, y & 15, z & 15);
    }
}
