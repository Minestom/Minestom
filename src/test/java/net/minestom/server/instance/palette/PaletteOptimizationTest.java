package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.Random;

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
        palette.setAll((x, y, z) -> random.nextInt(256));
        paletteEqualsOptimized(palette);
        palette.setAll((x, y, z) -> random.nextInt(2));
        paletteEqualsOptimized(palette);
    }

    @Test
    public void manualFill() {
        var palette = createPalette();
        palette.setAll((x, y, z) -> 1);
        paletteEqualsOptimized(palette);
        palette.setAll((x, y, z) -> 2);
        paletteEqualsOptimized(palette);
        palette.setAll((x, y, z) -> 0);
        paletteEqualsOptimized(palette);
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
