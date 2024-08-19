package net.minestom.server.instance.light;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static java.util.Map.entry;
import static net.minestom.server.instance.BlockLightMergeIntegrationTest.assertLightInstance;

@ExtendWith(MicrotusExtension.class)
class WorldRelightIntegrationTest {
    private @NotNull Instance createLightingInstance(@NotNull ServerProcess process) {
        var instance = process.instance().createInstanceContainer();
        instance.setGenerator(unit -> {
            unit.modifier().fillHeight(39, 40, Block.STONE);
            unit.subdivide().forEach(u -> u.modifier().setBlock(0, 10, 0, Block.GLOWSTONE));
            unit.modifier().fillHeight(50, 51, Block.STONE);
        });
        return instance;
    }

    @Test
    void testBorderLava(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        instance.loadChunk(6, 16).join();
        instance.loadChunk(6, 15).join();

        instance.setBlock(106, 70, 248, Block.LAVA);
        instance.setBlock(106, 71, 249, Block.LAVA);

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(105, 72, 256), 6)
        );

        LightingChunk.relight(instance, instance.getChunks());
        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testBlockRemoval(Env env) {
        Instance instance = createLightingInstance(env.process());
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        LightingChunk.relight(instance, instance.getChunks());

        var expectedLights = Map.ofEntries(
                entry(new Vec(-1, 40, 0), 12),
                entry(new Vec(-9, 40, 8), 0),
                entry(new Vec(-1, 40, -16), 12),
                entry(new Vec(-1, 37, 0), 3),
                entry(new Vec(-8, 37, -8), 0)
        );
        assertLightInstance(instance, expectedLights);
    }
}
