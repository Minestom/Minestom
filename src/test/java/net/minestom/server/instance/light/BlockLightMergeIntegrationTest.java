package net.minestom.server.instance.light;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.fail;

@EnvTest
public class BlockLightMergeIntegrationTest {
    @Test
    public void testPropagationAir(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y + 6, z);
                }
            }
        }
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testTorch(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testPropagationAir2(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testPropagationAirRemoval(Env env) {
        Instance instance = env.createFlatInstance();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        instance.setBlock(4, 100,8 , Block.TORCH);

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y + 6, z);
                }
            }
        }

        instance.setBlock(4, 100,8 , Block.AIR);

        Map<Vec, Integer> expectedLights = new HashMap<>();
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new Vec(8, 100 + y, 8), 0);
        }
        for (int y = -15; y <= 15; ++y) {
            expectedLights.put(new Vec(-2, 100 + y, 8), 0);
        }

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y + 6, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderOcclusion(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderOcclusion2(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderOcclusion3(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderCrossing(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBorderOcclusionRemoval(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        instance.setBlock(-2, 40, 4, Block.STONE);

        Map<Vec, Integer> expectedLights = Map.ofEntries(
                entry(new Vec(-2, 42, 4), 1),
                entry(new Vec(-2, 40, 2), 2),
                entry(new Vec(-4, 40, 4), 2)

        );

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testDiagonalRemoval(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }
        assertLightInstance(instance, expectedLights);

        instance.setBlock(-2, 40, 14, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new Vec(-2, 40, 14), 0),
                entry(new Vec(-2, 40, 18), 0),
                entry(new Vec(2, 40, 18), 0)

        );
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }
        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testDouble(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);

        instance.setBlock(-2, 40, 14, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new Vec(-4, 40, 25), 7),
                entry(new Vec(-4, 40, 18), 0)
        );

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    @Test
    public void testBlockRemoval(Env env) {
        Instance instance = env.createFlatInstance();
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

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);

        instance.setBlock(-1, 40, -1, Block.AIR);

        expectedLights = Map.ofEntries(
                entry(new Vec(-2, 40, -1), 13)
        );

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    ChunkUtils.updateSection(instance, x, y, z);
                }
            }
        }

        assertLightInstance(instance, expectedLights);
    }

    static byte lightVal(Instance instance, Vec pos) {
        final Vec modPos = new Vec(((pos.blockX() % 16) + 16) % 16, ((pos.blockY() % 16) + 16) % 16, ((pos.blockZ() % 16) + 16) % 16);
        Chunk chunk = instance.getChunkAt(pos.blockX(), pos.blockZ());
        return (byte) chunk.getSectionAt(pos.blockY()).blockLight().getLevel(modPos.blockX(), modPos.blockY(), modPos.blockZ());
    }

    static void assertLightInstance(Instance instance, Map<Vec, Integer> expectedLights) {
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
