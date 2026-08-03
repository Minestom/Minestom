package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaletteOptimizationTest {

    @Test
    public void empty() {
        var palette = createPalette();
        paletteEqualsOptimized(palette);
    }

    @Test
    public void single() {
        var palette = createPalette();
        palette.set(0, 0, 0, 1);
        paletteEqualsOptimized(palette);
    }

    @Test
    public void random() {
        var random = new Random(12345);
        var palette = createPalette();
        palette.setAll((_, _, _) -> random.nextInt(256));
        paletteEqualsOptimized(palette);
        palette.setAll((_, _, _) -> random.nextInt(2));
        paletteEqualsOptimized(palette);
    }

    @Test
    public void manualFill() {
        var palette = createPalette();
        palette.setAll((_, _, _) -> 1);
        paletteEqualsOptimized(palette);
        palette.setAll((_, _, _) -> 2);
        paletteEqualsOptimized(palette);
        palette.setAll((_, _, _) -> 0);
        paletteEqualsOptimized(palette);
    }

    @Test
    public void sizeOptimizationKeepsDirectPastIndirectCapacity() {
        var palette = createPalette();
        palette.setAll((x, y, z) -> (x | z << 4 | y << 8) % 300);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry());
        var optimized = (PaletteImpl) optimized(palette, Palette.Optimization.SIZE);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, optimized.bitsPerEntry);
        assertNull(optimized.table);
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    assertEquals((x | z << 4 | y << 8) % 300, optimized.get(x, y, z),
                            "Mismatch at (" + x + "," + y + "," + z + ")");
                }
            }
        }
    }

    @Test
    public void sizeOptimizationCollapsesUniformDirectToSingle() {
        var palette = createPalette();
        palette.setAll((x, _, _) -> x & 1);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry());
        palette.replace(1, 0);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry());
        var optimized = (PaletteImpl) optimized(palette, Palette.Optimization.SIZE);
        assertEquals(0, optimized.bitsPerEntry);
        assertEquals(0, optimized.singleValue);
        assertNull(optimized.values);
        assertNull(optimized.table);
    }

    PaletteImpl createPalette() {
        return (PaletteImpl) Palette.blocks();
    }

    Palette optimized(Palette palette, Palette.Optimization optimization) {
        palette = palette.clone();
        palette.optimize(optimization);
        return palette;
    }

    void paletteEqualsOptimized(Palette palette) {
        paletteEquals(palette, optimized(palette, Palette.Optimization.SIZE), true);
        paletteEquals(palette, optimized(palette, Palette.Optimization.SPEED), false);
    }

    void paletteEquals(Palette palette, Palette optimized, boolean sizeCompare) {
        assertTrue(palette.compare(optimized));
        if (sizeCompare) {
            var array = NetworkBuffer.makeArray(Palette.BLOCK_SERIALIZER, palette);
            int length1 = array.length;
            array = NetworkBuffer.makeArray(Palette.BLOCK_SERIALIZER, optimized);
            int length2 = array.length;
            assertTrue(length1 >= length2, "Optimized palette is bigger than the original one: " + length1 + " : " + length2);
        }
    }
}
