package net.minestom.server.instance;

import net.minestom.server.instance.palette.Palette;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockLightTest {

    @Test
    public void empty() {
        var palette = Palette.blocks();
        var result = BlockLight.compute(palette);
        // Verify section light
        for (byte light : result.light()) {
            assertEquals(0, light);
        }
        // Verify border light
        for (var side : BlockLight.Side.values()) {
            BlockLight.Border border = result.border(side);
            for (byte light : border.light()) {
                assertEquals(0, light);
            }
        }
    }
}
