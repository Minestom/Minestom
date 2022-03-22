package net.minestom.server.instance;

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

public class BlockLightTest {

    @Test
    public void empty() {
        var palette = Palette.blocks();
        var result = BlockLight.compute(palette);
        for (byte light : result.light()) {
            assertEquals(0, light);
        }
    }

    @Test
    public void glowstone() {
        var palette = Palette.blocks();
        palette.set(0, 1, 0, Block.GLOWSTONE.stateId());
        var result = BlockLight.compute(palette);
        assertLight(result, Map.of(
                new Vec(0, 1, 0), 15,
                new Vec(0, 1, 1), 14,
                new Vec(0, 1, 2), 13));
    }

    @Test
    public void glowstoneBorder() {
        var palette = Palette.blocks();
        palette.set(0, 1, 0, Block.GLOWSTONE.stateId());
        var result = BlockLight.compute(palette);
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
    public void glowstoneBlock() {
        var palette = Palette.blocks();
        palette.set(0, 1, 0, Block.GLOWSTONE.stateId());
        palette.set(0, 1, 1, Block.STONE.stateId());
        var result = BlockLight.compute(palette);
        assertLight(result, Map.of(
                new Vec(0, 1, 0), 15,
                new Vec(0, 1, 1), 0,
                new Vec(0, 1, 2), 11));
    }

    @Test
    public void isolated() {
        var palette = Palette.blocks();
        palette.set(4, 1, 4, Block.GLOWSTONE.stateId());

        palette.set(3, 1, 4, Block.STONE.stateId());
        palette.set(4, 1, 5, Block.STONE.stateId());
        palette.set(4, 1, 3, Block.STONE.stateId());
        palette.set(5, 1, 4, Block.STONE.stateId());
        palette.set(4, 2, 4, Block.STONE.stateId());
        palette.set(4, 0, 4, Block.STONE.stateId());

        var result = BlockLight.compute(palette);
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

    void assertLight(BlockLight.Result result, Map<Vec, Integer> expectedLights) {
        List<String> errors = new ArrayList<>();
        for (int x = -1; x < 17; x++) {
            for (int y = -1; y < 17; y++) {
                for (int z = -1; z < 17; z++) {
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
