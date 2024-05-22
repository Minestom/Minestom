package net.minestom.server.instance;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@EnvTest
public class BlockLightMergeIntegrationTest {
    @Test
    void testPropagationAir(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(8, 100,8 , Block.TORCH);

        Map<Vec, Integer> expectedLights = new HashMap<>();
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new Vec(8, 100 + y, 8), Math.max(0, 14 - Math.abs(y)));
        }

        LightingChunk.relightSection(instance, 0, 6, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testTorch(Env env) {
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

        instance.setBlock(1, 40,1 , Block.TORCH);

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(2, 40, 2), 12)
        );

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testTorch2(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(1, 40,1 , Block.TORCH);
        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(2, 40, 2), 12)
        );
        LightingChunk.relightSection(instance, 1, 2, 1);
        assertLightInstance(instance, expectedLights);

        instance.setBlock(-2, 40,-2, Block.TORCH);
        expectedLights = Map.ofEntries(
                entry(new Vec(2, 40, 2), 12)
        );
        LightingChunk.relightSection(instance, -1, 2, -1);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testPropagationAir2(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(4, 60,8 , Block.TORCH);

        Map<Vec, Integer> expectedLights = new HashMap<>();
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new Vec(8, 60 + y, 8), Math.max(0, 10 - Math.abs(y)));
        }
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new Vec(-2, 60 + y, 8), Math.max(0, 8 - Math.abs(y)));
        }

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testPropagationAirRemoval(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(4, 100,8 , Block.TORCH);

        LightingChunk.relightSection(instance, 0, 2, 0);

        instance.setBlock(4, 100,8 , Block.AIR);

        Map<Vec, Integer> expectedLights = new HashMap<>();
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new Vec(8, 100 + y, 8), 0);
        }
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new Vec(-2, 100 + y, 8), 0);
        }

        LightingChunk.relightSection(instance, 0, 6, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testBorderOcclusion(Env env) {
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

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(-2, 42, 4), 0),
                entry(new Vec(-2, 42, 3), 1),
                entry(new Vec(-2, 41, 3), 2),
                entry(new Vec(0, 40, 4), 2)
        );

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testBorderOcclusion2(Env env) {
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

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(-2, 42, 4), 8),
                entry(new Vec(-2, 40, 2), 8),
                entry(new Vec(-4, 40, 4), 4)

        );

        LightingChunk.relightSection(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testBorderOcclusion3(Env env) {
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

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(-2, 40, 7), 0)

        );

        LightingChunk.relightSection(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testBorderCrossing(Env env) {
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

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(-1, 40, 19), 2),
                entry(new Vec(0, 40, 19), 3),
                entry(new Vec(-1, 40, 16), 7),
                entry(new Vec(-1, 40, 13), 12),
                entry(new Vec(-1, 40, 7), 8),
                entry(new Vec(-3, 40, 4), 1),
                entry(new Vec(-3, 40, 5), 0),
                entry(new Vec(-1, 40, 20), 1)

        );

        LightingChunk.relightSection(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testBorderOcclusionRemoval(Env env) {
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

        LightingChunk.relightSection(instance, 0, 2, 0);

        instance.setBlock(-2, 40, 4, Block.STONE);

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(-2, 42, 4), 1),
                entry(new Vec(-2, 40, 2), 2),
                entry(new Vec(-4, 40, 4), 2)

        );

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    void chunkIntersection(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = 4; x <= 7; x++) {
            for (int z = 6; z <= 8; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(94, -35, 128, Block.GLOW_LICHEN.withProperties(Map.of("west", "true")));

        LightingChunk.relight(instance, instance.getChunks());

        var val = instance.getChunk(5, 8).getSection(-2).blockLight().getLevel(14, 0, 0);
        assertEquals(4, val);

        var val2 = instance.getChunk(5, 8).getSection(-3).blockLight().getLevel(14, 15, 0);
        assertEquals(5, val2);
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
    void skylight(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = 4; x <= 7; x++) {
            for (int z = 6; z <= 8; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(94, 50, 128, Block.STONE);

        LightingChunk.relight(instance, instance.getChunks());

        var val = lightValSky(instance, new Vec(94, 41, 128));
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

        LightingChunk.relight(instance, instance.getChunks());

        var val = lightValSky(instance, new Vec(94, 50, 128));
        assertEquals(15, val);
    }

    @Test
    void skylightContained(Env env) {
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

        LightingChunk.relight(instance, instance.getChunks());

        var val = lightValSky(instance, new Vec(94, 51, 128));
        var val2 = lightValSky(instance, new Vec(94, 52, 128));
        assertEquals(0, val2);
        assertEquals(0, val);
    }

    @Test
    void testDiagonalRemoval(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(-2, 40, 14, Block.TORCH);

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(-2, 40, 14), 14),
                entry(new Vec(-2, 40, 18), 10),
                entry(new Vec(2, 40, 18), 6)

        );

        LightingChunk.relightSection(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);

        instance.setBlock(-2, 40, 14, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new Vec(-2, 40, 14), 0),
                entry(new Vec(-2, 40, 18), 0),
                entry(new Vec(2, 40, 18), 0)

        );
        LightingChunk.relightSection(instance, 0, 2, 0);
        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testDiagonalRemoval2(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setChunkSupplier(LightingChunk::new);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(1, 40, 1, Block.TORCH);
        instance.setBlock(1, 40, 17, Block.TORCH);

        LightingChunk.relightSection(instance, 0, 2, 0);

        instance.setBlock(1, 40, 17, Block.AIR);

        var expectedLights = Map.ofEntries(
                entry(new Vec(-3, 40, 2), 9)
        );

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testDouble(Env env) {
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
                entry(new Vec(-4, 40, 25), 7),
                entry(new Vec(-4, 40, 18), 8)
        );

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);

        instance.setBlock(-2, 40, 14, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new Vec(-4, 40, 25), 7),
                entry(new Vec(-4, 40, 18), 0)
        );

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    @Test
    void testBlockRemoval(Env env) {
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
                entry(new Vec(-2, 40, -1), 0)
        );

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);

        instance.setBlock(-1, 40, -1, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new Vec(-2, 40, -1), 13)
        );

        LightingChunk.relightSection(instance, 0, 2, 0);

        assertLightInstance(instance, expectedLights);
    }

    static byte lightVal(Instance instance, Vec pos) {
        final Vec modPos = new Vec(((pos.blockX() % 16) + 16) % 16, ((pos.blockY() % 16) + 16) % 16, ((pos.blockZ() % 16) + 16) % 16);
        Chunk chunk = instance.getChunkAt(pos.blockX(), pos.blockZ());
        return (byte) chunk.getSectionAt(pos.blockY()).blockLight().getLevel(modPos.blockX(), modPos.blockY(), modPos.blockZ());
    }

    static byte lightValSky(Instance instance, Vec pos) {
        final Vec modPos = new Vec(((pos.blockX() % 16) + 16) % 16, ((pos.blockY() % 16) + 16) % 16, ((pos.blockZ() % 16) + 16) % 16);
        Chunk chunk = instance.getChunkAt(pos.blockX(), pos.blockZ());
        return (byte) chunk.getSectionAt(pos.blockY()).skyLight().getLevel(modPos.blockX(), modPos.blockY(), modPos.blockZ());
    }

    public static void assertLightInstance(Instance instance, Map<Vec, Integer> expectedLights) {
        List<String> errors = new ArrayList<>();
        for (var entry : expectedLights.entrySet()) {
            final Integer expected = entry.getValue();
            final Vec pos = entry.getKey();

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
}