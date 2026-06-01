package net.minestom.server.instance.light;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Map.entry;
import static net.minestom.server.instance.BlockLightMergeIntegrationTest.assertLightInstance;

@Disabled("enable or delete once lighting is complete")
@EnvTest
public class WorldRelightIntegrationTest {
    private Instance createLightingInstance(ServerProcess process) {
        var instance = process.instance().createInstanceContainer();
        instance.setGenerator(unit -> {
            unit.modifier().fillHeight(39, 40, Block.STONE);
            unit.subdivide().forEach(u -> u.modifier().setBlock(0, 10, 0, Block.GLOWSTONE));
            unit.modifier().fillHeight(50, 51, Block.STONE);
        });
        return instance;
    }

    @Test
    public void testBorderLava(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        instance.loadChunk(6, 16).join();
        instance.loadChunk(6, 15).join();

        instance.setBlock(106, 70, 248, Block.LAVA);
        instance.setBlock(106, 71, 249, Block.LAVA);

        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(105, 72, 256), 6)
        );

        if (true) throw new AssertionError();
//        LightingChunk.relight(instance, instance.getChunks());
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBlockRemoval(Env env) {
        Instance instance = createLightingInstance(env.process());
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        if (true) throw new AssertionError();
//        LightingChunk.relight(instance, instance.getChunks());

        var expectedLights = Map.ofEntries(
                entry(new BlockVec(-1, 40, 0), 12),
                entry(new BlockVec(-9, 40, 8), 0),
                entry(new BlockVec(-1, 40, -16), 12),
                entry(new BlockVec(-1, 37, 0), 3),
                entry(new BlockVec(-8, 37, -8), 0)
        );
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testJackOLantern(Env env) {
        Instance instance = createLightingInstance(env.process());
        instance.setChunkSupplier(LightingChunk::new);

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(10, 60, 10, Block.JACK_O_LANTERN);
        if (true) throw new AssertionError();
//        LightingChunk.relight(instance, instance.getChunks());

        var expectedLights = Map.ofEntries(
                entry(new BlockVec(11, 60, 10), 14),
                entry(new BlockVec(10, 61, 10), 14),
                entry(new BlockVec(15, 60, 10), 10)
        );

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testRedstoneLamp(Env env) {
        Instance instance = createLightingInstance(env.process());
        instance.setChunkSupplier(LightingChunk::new);

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(10, 60, 10, Block.REDSTONE_LAMP.withProperty("lit", "true"));
        if (true) throw new AssertionError();
//        LightingChunk.relight(instance, instance.getChunks());

        var expectedLights = Map.ofEntries(
                entry(new BlockVec(11, 60, 10), 14),
                entry(new BlockVec(10, 61, 10), 14),
                entry(new BlockVec(15, 60, 10), 10)
        );

        assertLightInstance(instance, expectedLights);
    }
}
