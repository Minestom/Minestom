package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class BlockLightTest {

    @Test
    void empty() {
        var palette = Palette.blocks();
        var result = LightCompute.compute(palette);
        for (byte light : result.light()) {
            assertEquals(0, light);
        }
    }

    @Test
    void glowstone() {
        var palette = Palette.blocks();
        palette.set(0, 1, 0, Block.GLOWSTONE.stateId());
        var result = LightCompute.compute(palette);
        assertLight(result, Map.of(
                new Vec(0, 1, 0), 15,
                new Vec(0, 1, 1), 14,
                new Vec(0, 1, 2), 13));
    }

    @Test
    void doubleGlowstone() {
        var palette = Palette.blocks();
        palette.set(0, 1, 0, Block.GLOWSTONE.stateId());
        palette.set(4, 1, 4, Block.GLOWSTONE.stateId());

        var result = LightCompute.compute(palette);
        assertLight(result, Map.of(
                new Vec(1, 1, 3), 11,
                new Vec(3, 3, 7), 9,
                new Vec(1, 1, 1), 13,
                new Vec(3, 1, 4), 14));
    }

    @Test
    void glowstoneBorder() {
        var palette = Palette.blocks();
        palette.set(0, 1, 0, Block.GLOWSTONE.stateId());
        var result = LightCompute.compute(palette);
        assertLight(result, Map.of(
                // X axis
                new Vec(-1, 0, 0), 13,
                new Vec(-1, 1, 0), 14,
                new Vec(-1, 2, 0), 13,
                new Vec(-1, 3, 0), 12,
                // Z axis
                new Vec(0, 0, -1), 13,
                new Vec(0, 1, -1), 14,
                new Vec(0, 2, -1), 13,
                new Vec(0, 3, -1), 12));
    }

    @Test
    void glowstoneBlock() {
        var palette = Palette.blocks();
        palette.set(0, 1, 0, Block.GLOWSTONE.stateId());
        palette.set(0, 1, 1, Block.STONE.stateId());
        var result = LightCompute.compute(palette);
        assertLight(result, Map.of(
                new Vec(0, 1, 0), 15,
                new Vec(0, 1, 1), 0,
                new Vec(0, 1, 2), 11));
    }

    @Test
    void isolated() {
        var palette = Palette.blocks();
        palette.set(4, 1, 4, Block.GLOWSTONE.stateId());

        palette.set(3, 1, 4, Block.STONE.stateId());
        palette.set(4, 1, 5, Block.STONE.stateId());
        palette.set(4, 1, 3, Block.STONE.stateId());
        palette.set(5, 1, 4, Block.STONE.stateId());
        palette.set(4, 2, 4, Block.STONE.stateId());
        palette.set(4, 0, 4, Block.STONE.stateId());

        var result = LightCompute.compute(palette);
        assertLight(result, Map.ofEntries(
                // Glowstone
                entry(new Vec(4, 1, 4), 15),
                // Isolation
                entry(new Vec(3, 1, 4), 0),
                entry(new Vec(4, 1, 5), 0),
                entry(new Vec(4, 1, 3), 0),
                entry(new Vec(5, 1, 4), 0),
                entry(new Vec(4, 2, 4), 0),
                entry(new Vec(4, 0, 4), 0),
                // Outside location
                entry(new Vec(2, 2, 3), 0)));
    }

    @Test
    void isolatedStair() {
        var palette = Palette.blocks();
        palette.set(4, 1, 4, Block.GLOWSTONE.stateId());
        palette.set(3, 1, 4, Block.OAK_STAIRS.withProperties(Map.of(
                "facing", "east",
                "half", "bottom",
                "shape", "straight")).stateId());
        palette.set(4, 1, 5, Block.STONE.stateId());
        palette.set(4, 1, 3, Block.STONE.stateId());
        palette.set(5, 1, 4, Block.STONE.stateId());
        palette.set(4, 2, 4, Block.STONE.stateId());
        palette.set(4, 0, 4, Block.STONE.stateId());

        var result = LightCompute.compute(palette);
        assertLight(result, Map.ofEntries(
                // Glowstone
                entry(new Vec(4, 1, 4), 15),
                // Front of stair
                entry(new Vec(2, 1, 4), 0)));
    }

    @Test
    void isolatedStairOpposite() {
        var palette = Palette.blocks();
        palette.set(4, 1, 4, Block.GLOWSTONE.stateId());
        palette.set(3, 1, 4, Block.OAK_STAIRS.withProperties(Map.of(
                "facing", "west",
                "half", "bottom",
                "shape", "straight")).stateId());
        palette.set(4, 1, 5, Block.STONE.stateId());
        palette.set(4, 1, 3, Block.STONE.stateId());
        palette.set(5, 1, 4, Block.STONE.stateId());
        palette.set(4, 2, 4, Block.STONE.stateId());
        palette.set(4, 0, 4, Block.STONE.stateId());

        var result = LightCompute.compute(palette);
        assertLight(result, Map.ofEntries(
                // Glowstone
                entry(new Vec(4, 1, 4), 15),
                // Stair
                entry(new Vec(3, 1, 4), 14),
                // Front of stair
                entry(new Vec(2, 1, 4), 11),
                // Others
                entry(new Vec(3, 0, 5), 12),
                entry(new Vec(3, 0, 3), 12)));
    }

    @Test
    void isolatedStairWest() {
        var palette = Palette.blocks();
        palette.set(4, 1, 4, Block.GLOWSTONE.stateId());
        palette.set(3, 1, 4, Block.OAK_STAIRS.withProperties(Map.of(
                "facing", "west",
                "half", "bottom",
                "shape", "straight")).stateId());
        palette.set(4, 1, 5, Block.STONE.stateId());
        palette.set(4, 1, 3, Block.STONE.stateId());
        palette.set(5, 1, 4, Block.STONE.stateId());
        palette.set(4, 2, 4, Block.STONE.stateId());
        palette.set(4, 0, 4, Block.STONE.stateId());

        var result = LightCompute.compute(palette);
        assertLight(result, Map.ofEntries(
                // Glowstone
                entry(new Vec(4, 1, 4), 15),
                // Stair
                entry(new Vec(3, 1, 4), 14),
                // Front of stair
                entry(new Vec(2, 1, 4), 11),
                // Others
                entry(new Vec(3, 0, 5), 12),
                entry(new Vec(3, 0, 3), 12),
                entry(new Vec(3, 2, 4), 13),
                entry(new Vec(3, -1, 4), 10),
                entry(new Vec(2, 0, 4), 10)));
    }

    @Test
    void isolatedStairSouth() {
        var palette = Palette.blocks();
        palette.set(4, 1, 4, Block.GLOWSTONE.stateId());
        palette.set(3, 1, 4, Block.OAK_STAIRS.withProperties(Map.of(
                "facing", "south",
                "half", "bottom",
                "shape", "straight")).stateId());
        palette.set(4, 1, 5, Block.STONE.stateId());
        palette.set(4, 1, 3, Block.STONE.stateId());
        palette.set(5, 1, 4, Block.STONE.stateId());
        palette.set(4, 2, 4, Block.STONE.stateId());
        palette.set(4, 0, 4, Block.STONE.stateId());

        var result = LightCompute.compute(palette);
        assertLight(result, Map.ofEntries(
                // Glowstone
                entry(new Vec(4, 1, 4), 15),
                // Stair
                entry(new Vec(3, 1, 4), 14),
                // Front of stair
                entry(new Vec(2, 1, 4), 13),
                // Others
                entry(new Vec(3, 0, 5), 10),
                entry(new Vec(3, 0, 3), 12)));
    }

    void assertLight(LightCompute.Result result, Map<Vec, Integer> expectedLights) {
        List<String> errors = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    var expected = expectedLights.get(new Vec(x, y, z));
                    if (expected != null) {
                        final byte light = result.getLight(x, y, z);
                        if (light != expected) {
                            errors.add(String.format("Expected %d at [%d,%d,%d] but got %d", expected, x, y, z, light));
                        }
                    }
                }
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
