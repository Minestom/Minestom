package net.minestom.server.instance.light;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LightEngineTest {

    /**
     * A minimal in-memory world: sections in a map, no chunks or instances involved - the {@link LightEngine.WorldView}
     * contract a virtual world implements.
     */
    private static class VirtualWorld implements LightEngine.WorldView {
        final Map<Vec, Palette> palettes = new HashMap<>();
        final Map<Vec, Light> blockLights = new HashMap<>();
        final Map<Vec, Light> skyLights = new HashMap<>();
        final int[] openColumn = new int[16 * 16]; // all zero: nothing occludes the sky

        Palette section(int chunkX, int sectionY, int chunkZ) {
            var key = new Vec(chunkX, sectionY, chunkZ);
            blockLights.computeIfAbsent(key, k -> Light.block());
            skyLights.computeIfAbsent(key, k -> Light.sky());
            return palettes.computeIfAbsent(key, k -> Palette.blocks());
        }

        @Override
        public @Nullable Light light(LightEngine.Type type, int chunkX, int sectionY, int chunkZ) {
            var key = new Vec(chunkX, sectionY, chunkZ);
            return type == LightEngine.Type.BLOCK ? blockLights.get(key) : skyLights.get(key);
        }

        @Override
        public @Nullable Palette palette(int chunkX, int sectionY, int chunkZ) {
            return palettes.get(new Vec(chunkX, sectionY, chunkZ));
        }

        @Override
        public int @Nullable [] occlusionMap(int chunkX, int chunkZ) {
            return openColumn;
        }

        @Override
        public int maxY() {
            return 64;
        }
    }

    @Test
    public void borderBrighteningQueuesUnlistedSections() {
        var world = new VirtualWorld();
        // corner glowstone: light entering the east neighbor lands on its south border and must queue the
        // diagonal section, which the initial queue never listed (direct neighbors are the caller's job,
        // like LightingChunk#invalidateNeighborsSection - a single emitter cannot out-travel a section)
        world.section(0, 0, 0).set(15, 8, 15, Block.GLOWSTONE.stateId());
        world.section(1, 0, 0);
        world.section(1, 0, 1);

        var touched = LightEngine.relight(world, Set.of(new Vec(0, 0, 0), new Vec(1, 0, 0)), LightEngine.Type.BLOCK);
        assertTrue(touched.contains(new BlockVec(1, 0, 1)), "the brightened border queues the diagonal section");
        // an internal pass nulls its neighbors' propagation, so a relight always queues the whole dirty
        // neighborhood (what collectRequiredNearby collects for instances) - then the diagonal exposes its level
        LightEngine.relight(world, Set.of(new Vec(0, 0, 0), new Vec(1, 0, 0), new Vec(1, 0, 1)), LightEngine.Type.BLOCK);
        assertEquals(13, world.light(LightEngine.Type.BLOCK, 1, 0, 1).getLevel(0, 8, 0));
    }

    @Test
    public void lightCrossesSectionBorders() {
        var world = new VirtualWorld();
        world.section(0, 0, 0).set(15, 8, 8, Block.GLOWSTONE.stateId());
        world.section(1, 0, 0);

        LightEngine.relight(world, Set.of(new Vec(0, 0, 0), new Vec(1, 0, 0)), LightEngine.Type.BLOCK);
        var source = world.light(LightEngine.Type.BLOCK, 0, 0, 0);
        var neighbor = world.light(LightEngine.Type.BLOCK, 1, 0, 0);
        assertEquals(15, source.getLevel(15, 8, 8));
        assertEquals(14, neighbor.getLevel(0, 8, 8), "light continues across the section border");
        assertEquals(13, neighbor.getLevel(1, 8, 8));
    }

    @Test
    public void recomputesGateContainsTheWave() {
        // the same corner setup as above, but the diagonal section opts out of recomputing
        var world = new VirtualWorld() {
            @Override
            public boolean recomputes(int chunkX, int sectionY, int chunkZ) {
                return chunkZ == 0;
            }
        };
        world.section(0, 0, 0).set(15, 8, 15, Block.GLOWSTONE.stateId());
        world.section(1, 0, 0);
        world.section(1, 0, 1);

        var touched = LightEngine.relight(world, Set.of(new Vec(0, 0, 0), new Vec(1, 0, 0)), LightEngine.Type.BLOCK);
        assertTrue(touched.contains(new BlockVec(1, 0, 0)));
        assertFalse(touched.contains(new BlockVec(1, 0, 1)), "a non-recomputing section stops the wave");
        assertEquals(0, world.light(LightEngine.Type.BLOCK, 1, 0, 1).getLevel(0, 8, 0));
    }

    @Test
    public void skyLightFloodsAnOpenColumn() {
        var world = new VirtualWorld();
        world.section(0, 0, 0);

        LightEngine.relight(world, Set.of(new Vec(0, 0, 0)), LightEngine.Type.SKY);
        var sky = world.light(LightEngine.Type.SKY, 0, 0, 0);
        assertEquals(15, sky.getLevel(0, 0, 0));
        assertEquals(15, sky.getLevel(8, 15, 8));
    }

    @Test
    public void occlusionMatchesTheDiffuseSet() {
        assertTrue(LightEngine.checkSkyOcclusion(Block.STONE));
        assertTrue(LightEngine.checkSkyOcclusion(Block.WATER), "diffuse blocks occlude despite their shape");
        assertTrue(LightEngine.checkSkyOcclusion(Block.OAK_LEAVES));
        assertFalse(LightEngine.checkSkyOcclusion(Block.AIR));
        assertFalse(LightEngine.checkSkyOcclusion(Block.OAK_FENCE));
    }
}
