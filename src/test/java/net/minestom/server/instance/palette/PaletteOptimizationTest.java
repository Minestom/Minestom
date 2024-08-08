package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaletteOptimizationTest {

    @Test
    public void empty() {
        var palette = createPalette();
        paletteEquals(palette.palette, palette.optimizedPalette());
    }

    @Test
    public void single() {
        var palette = createPalette();
        palette.set(0, 0, 0, 1);
        paletteEquals(palette.palette, palette.optimizedPalette());
    }

    @Test
    public void random() {
        var random = new Random(12345);
        var palette = createPalette();
        palette.setAll((x, y, z) -> random.nextInt(256));
        paletteEquals(palette.palette, palette.optimizedPalette());
        palette.setAll((x, y, z) -> random.nextInt(2));
        paletteEquals(palette.palette, palette.optimizedPalette());
    }

    @Test
    public void manualFill() {
        var palette = createPalette();
        palette.setAll((x, y, z) -> 1);
        paletteEquals(palette.palette, palette.optimizedPalette());
        palette.setAll((x, y, z) -> 2);
        paletteEquals(palette.palette, palette.optimizedPalette());
        palette.setAll((x, y, z) -> 0);
        paletteEquals(palette.palette, palette.optimizedPalette());
    }

    AdaptivePalette createPalette() {
        return (AdaptivePalette) Palette.blocks();
    }

    void paletteEquals(Palette palette, Palette optimized) {
        // Verify content
        assertEquals(palette.dimension(), optimized.dimension());
        for (int y = 0; y < palette.dimension(); y++) {
            for (int z = 0; z < palette.dimension(); z++) {
                for (int x = 0; x < palette.dimension(); x++) {
                    assertEquals(palette.get(x, y, z), optimized.get(x, y, z));
                }
            }
        }
        // Verify size
        {
            var array = NetworkBuffer.makeArray(Palette.BLOCK_SERIALIZER, palette);
            int length1 = array.length;
            array = NetworkBuffer.makeArray(Palette.BLOCK_SERIALIZER, optimized);
            int length2 = array.length;
            assertTrue(length1 >= length2, "Optimized palette is bigger than the original one: " + length1 + " : " + length2);
        }
    }
}
