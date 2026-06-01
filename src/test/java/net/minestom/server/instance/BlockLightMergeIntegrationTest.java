package net.minestom.server.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.light.LightingChunk;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.locks.LockSupport;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled("enable or delete once lighting is complete")
@EnvTest
public class BlockLightMergeIntegrationTest {
    @Test
    public void testPropagationAir(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(8, 100, 8, Block.TORCH);

        Map<BlockVec, Integer> expectedLights = new HashMap<>();
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new BlockVec(8, 100 + y, 8), Math.max(0, 14 - Math.abs(y)));
        }

        awaitLight(instance, 0, 6, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testTorch(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        instance.setGenerator(unit -> {
            unit.modifier().fillHeight(39, 40, Block.STONE);
            unit.modifier().fillHeight(50, 51, Block.STONE);
        });

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(1, 40, 1, Block.TORCH);

        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(2, 40, 2), 12)
        );

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testTorch2(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(1, 40, 1, Block.TORCH);
        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(2, 40, 2), 12)
        );
        awaitLight(instance, 1, 2, 1);
        assertLightInstance(instance, expectedLights);

        instance.setBlock(-2, 40, -2, Block.TORCH);
        expectedLights = Map.ofEntries(
                entry(new BlockVec(2, 40, 2), 12)
        );
        awaitLight(instance, -1, 2, -1);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testPropagationAir2(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(4, 60, 8, Block.TORCH);

        Map<BlockVec, Integer> expectedLights = new HashMap<>();
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new BlockVec(8, 60 + y, 8), Math.max(0, 10 - Math.abs(y)));
        }
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new BlockVec(-2, 60 + y, 8), Math.max(0, 8 - Math.abs(y)));
        }

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testPropagationAirRemoval(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(4, 100, 8, Block.TORCH);

        awaitLight(instance, 0, 2, 0);

        instance.setBlock(4, 100, 8, Block.AIR);

        Map<BlockVec, Integer> expectedLights = new HashMap<>();
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new BlockVec(8, 100 + y, 8), 0);
        }
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new BlockVec(-2, 100 + y, 8), 0);
        }

        awaitLight(instance, 0, 6, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderOcclusion(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(-1, 40, 4, Block.MAGMA_BLOCK);
        instance.setBlock(-1, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 4, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-1, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 41, 4, Block.STONE);
        instance.setBlock(-2, 40, 4, Block.TORCH);

        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(-2, 42, 4), 0),
                entry(new BlockVec(-2, 42, 3), 1),
                entry(new BlockVec(-2, 41, 3), 2),
                entry(new BlockVec(0, 40, 4), 2)
        );

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderOcclusion2(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(-1, 41, 4, Block.MAGMA_BLOCK);
        instance.setBlock(-1, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 4, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-1, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 41, 4, Block.STONE);
        instance.setBlock(-2, 40, 4, Block.TORCH);

        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(-2, 42, 4), 8),
                entry(new BlockVec(-2, 40, 2), 8),
                entry(new BlockVec(-4, 40, 4), 4)

        );

        awaitLight(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderOcclusion3(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(0, 40, 8, Block.STONE);
        instance.setBlock(1, 40, 8, Block.STONE);
        instance.setBlock(0, 41, 7, Block.STONE);
        instance.setBlock(1, 41, 7, Block.STONE);
        instance.setBlock(2, 40, 7, Block.STONE);
        instance.setBlock(1, 40, 6, Block.STONE);
        instance.setBlock(0, 40, 6, Block.STONE);

        instance.setBlock(1, 40, 7, Block.TORCH);
        instance.setBlock(0, 40, 7, Block.SANDSTONE_SLAB.withProperty("type", "bottom"));
        instance.setBlock(-1, 40, 7, Block.SANDSTONE_SLAB.withProperty("type", "top"));

        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(-2, 40, 7), 0)

        );

        awaitLight(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderCrossing(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        for (int x = -2; x <= 1; ++x) {
            for (int z = 5; z <= 20; ++z) {
                instance.setBlock(x, 42, z, Block.STONE);
            }
        }

        for (int z = 5; z <= 20; ++z) {
            for (int y = 40; y <= 42; ++y) {
                instance.setBlock(1, y, z, Block.STONE);
                instance.setBlock(-2, y, z, Block.STONE);
            }
        }

        for (int y = 40; y <= 42; ++y) {
            instance.setBlock(-1, y, 6, Block.STONE);
            instance.setBlock(0, y, 8, Block.STONE);
            instance.setBlock(-1, y, 10, Block.STONE);
            instance.setBlock(0, y, 12, Block.STONE);
            instance.setBlock(-1, y, 14, Block.STONE);
            instance.setBlock(0, y, 16, Block.STONE);
            instance.setBlock(-1, y, 18, Block.STONE);
            instance.setBlock(0, y, 20, Block.STONE);
        }

        instance.setBlock(-1, 40, 11, Block.TORCH);

        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(-1, 40, 19), 2),
                entry(new BlockVec(0, 40, 19), 3),
                entry(new BlockVec(-1, 40, 16), 7),
                entry(new BlockVec(-1, 40, 13), 12),
                entry(new BlockVec(-1, 40, 7), 8),
                entry(new BlockVec(-3, 40, 4), 1),
                entry(new BlockVec(-3, 40, 5), 0),
                entry(new BlockVec(-1, 40, 20), 1)

        );

        awaitLight(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderOcclusionRemoval(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(-1, 41, 4, Block.MAGMA_BLOCK);
        instance.setBlock(-1, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 3, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 4, Block.MAGMA_BLOCK);
        instance.setBlock(-3, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-1, 40, 5, Block.MAGMA_BLOCK);
        instance.setBlock(-2, 41, 4, Block.STONE);


        instance.setBlock(-2, 40, 4, Block.TORCH);

        awaitLight(instance, 0, 2, 0);

        instance.setBlock(-2, 40, 4, Block.STONE);

        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(-2, 42, 4), 1),
                entry(new BlockVec(-2, 40, 2), 2),
                entry(new BlockVec(-4, 40, 4), 2)

        );

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void chunkIntersection(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = 4; x <= 7; x++) {
            for (int z = 6; z <= 8; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(94, -35, 128, Block.GLOW_LICHEN.withProperties(Map.of("west", "true")));

        awaitLight(instance.getChunks());

        assertEquals(4, instance.getBlockLight(94, -32, 128));
        assertEquals(5, instance.getBlockLight(94, -33, 128));
    }

    @Test
    public void lightLookupTest(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = 4; x <= 7; x++) {
            for (int z = 6; z <= 8; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(94, -35, 128, Block.GLOW_LICHEN.withProperties(Map.of("west", "true")));

        var val = instance.getBlockLight(94, -35, 128);
        assertEquals(7, val);

        var val2 = instance.getBlockLight(94, -36, 128);
        assertEquals(6, val2);

        var val3 = instance.getSkyLight(94, -34, 128);
        assertEquals(0, val3);

        var val4 = instance.getSkyLight(94, 41, 128);
        assertEquals(15, val4);
    }

    @Test
    public void lightLookupTestCrossBorder(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = 4; x <= 7; x++) {
            for (int z = 6; z <= 8; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(94, -35, 128, Block.GLOWSTONE);

        var val = instance.getBlockLight(94, -35, 128);
        assertEquals(15, val);

        var val2 = instance.getBlockLight(97, -36, 135);
        assertEquals(4, val2);
    }

    @Test
    public void skylight(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = 4; x <= 7; x++) {
            for (int z = 6; z <= 8; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        System.out.println("set block");
        instance.setBlock(94, 50, 128, Block.STONE);

        awaitLight(instance.getChunks());

        LockSupport.parkNanos(1000000000);
        var val = lightValSky(instance, new BlockVec(94, 41, 128));
        assertEquals(14, val);
    }

    @Test
    public void skylightShortGrass(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = 4; x <= 7; x++) {
            for (int z = 6; z <= 8; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(94, 50, 128, Block.SHORT_GRASS);

        awaitLight(instance.getChunks());

        var val = lightValSky(instance, new BlockVec(94, 50, 128));
        assertEquals(15, val);
    }

    @Test
    public void skylightContained(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = 4; x <= 7; x++) {
            for (int z = 6; z <= 8; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(94, 50, 128, Block.STONE);
        instance.setBlock(94, 52, 128, Block.STONE);

        instance.setBlock(94, 51, 127, Block.STONE);
        instance.setBlock(94, 51, 129, Block.STONE);
        instance.setBlock(93, 51, 128, Block.STONE);
        instance.setBlock(95, 51, 128, Block.STONE);

        awaitLight(instance.getChunks());

        var val = lightValSky(instance, new BlockVec(94, 51, 128));
        var val2 = lightValSky(instance, new BlockVec(94, 52, 128));
        assertEquals(0, val2);
        assertEquals(0, val);
    }

    @Test
    public void testDiagonalRemoval(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(-2, 40, 14, Block.TORCH);

        Map<BlockVec, Integer> expectedLights = Map.ofEntries(
                entry(new BlockVec(-2, 40, 14), 14),
                entry(new BlockVec(-2, 40, 18), 10),
                entry(new BlockVec(2, 40, 18), 6)

        );

        awaitLight(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);

        instance.setBlock(-2, 40, 14, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new BlockVec(-2, 40, 14), 0),
                entry(new BlockVec(-2, 40, 18), 0),
                entry(new BlockVec(2, 40, 18), 0)

        );
        awaitLight(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testDiagonalRemoval2(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(1, 40, 1, Block.TORCH);
        instance.setBlock(1, 40, 17, Block.TORCH);

        awaitLight(instance, 0, 2, 0);

        instance.setBlock(1, 40, 17, Block.AIR);

        var expectedLights = Map.ofEntries(
                entry(new BlockVec(-3, 40, 2), 9)
        );

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testDouble(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(-2, 40, 14, Block.TORCH);
        instance.setBlock(1, 40, 27, Block.TORCH);

        var expectedLights = Map.ofEntries(
                entry(new BlockVec(-4, 40, 25), 7),
                entry(new BlockVec(-4, 40, 18), 8)
        );

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);

        instance.setBlock(-2, 40, 14, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new BlockVec(-4, 40, 25), 7),
                entry(new BlockVec(-4, 40, 18), 0)
        );

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBlockRemoval(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(0, 40, 0, Block.STONE);
        instance.setBlock(1, 40, -1, Block.STONE);
        instance.setBlock(0, 40, -2, Block.STONE);
        instance.setBlock(-1, 40, -1, Block.STONE);
        instance.setBlock(0, 41, -1, Block.STONE);
        instance.setBlock(0, 40, -1, Block.GLOWSTONE);

        var expectedLights = Map.ofEntries(
                entry(new BlockVec(-2, 40, -1), 0)
        );

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);

        instance.setBlock(-1, 40, -1, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new BlockVec(-2, 40, -1), 13)
        );

        awaitLight(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    static byte lightVal(Instance instance, BlockVec pos) {
        return (byte) instance.getBlockLight(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    static byte lightValSky(Instance instance, BlockVec pos) {
        return (byte) instance.getSkyLight(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    public static void assertLightInstance(Instance instance, Map<BlockVec, Integer> expectedLights) {
        List<String> errors = new ArrayList<>();
        for (var entry : expectedLights.entrySet()) {
            final Integer expected = entry.getValue();
            final BlockVec pos = entry.getKey();

            final byte light = lightVal(instance, pos);

            if (light != expected) {
                String errorLine = String.format("Expected %d at [%d,%d,%d] but got %d", expected, pos.blockX(), pos.blockY(), pos.blockZ(), light);
                System.err.println();
                errors.add(errorLine);
            }
        }
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : errors) {
                sb.append(s).append("\n");
            }
            System.err.println(sb);
            fail();
        }
    }

    // We shouldn't have to wait for light, it should automatically update
    private static void awaitLight(Collection<? extends Chunk> chunks) {
//        for (var chunk : chunks) {
//            ((LightingChunk) chunk).awaitLight();
//        }
    }

    // We shouldn't have to wait for light, it should automatically update
    private static void awaitLight(Instance instance, int sectionX, int sectionY, int sectionZ) {
//        var chunk = (LightingChunk) instance.getChunk(sectionX, sectionZ);
//        chunk.awaitLight();
//        var chunk = (LightingChunk) instance.getChunk(sectionX, sectionZ);
//        Objects.requireNonNull(chunk).getLightSection(sectionY).awaitLight();
    }
}