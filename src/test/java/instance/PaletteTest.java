package instance;

import net.minestom.server.instance.palette.Palette;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaletteTest {
    private List<Palette> palettes = new ArrayList<>();

    @BeforeEach
    public void reset() {
        for (int i = 4; i < 16; i++) {
            palettes.add(Palette.newPalette(i, 5, 3, 1));
        }
    }

    @Test
    public void testPlacement() {
        for (Palette palette : palettes) {
            final int dimension = palette.dimension();
            assertEquals(0, palette.get(0, 0, 0), "Default value should be 0");
            assertEquals(0, palette.size());
            palette.set(0, 0, 0, 64);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(64, palette.get(dimension, 0, 0), "Coordinate must be rounded to the palette dimension");
            assertEquals(1, palette.size());

            palette.set(1, 0, 0, 65);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(2, palette.size());

            palette.set(0, 1, 0, 66);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(66, palette.get(0, 1, 0));
            assertEquals(3, palette.size());

            palette.set(0, 0, 1, 67);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(66, palette.get(0, 1, 0));
            assertEquals(67, palette.get(0, 0, 1));
            assertEquals(4, palette.size());

            palette.set(0, 0, 1, 68);
            assertEquals(4, palette.size());
        }
    }

    @Test
    public void testPlacementNeg() {
        for (Palette palette : palettes) {
            assertThrows(IllegalArgumentException.class, () -> palette.set(-1, 0, 0, 64));
            assertThrows(IllegalArgumentException.class, () -> palette.set(0, -1, 0, 64));
            assertThrows(IllegalArgumentException.class, () -> palette.set(0, 0, -1, 64));

            assertThrows(IllegalArgumentException.class, () -> palette.get(-1, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> palette.get(0, -1, 0));
            assertThrows(IllegalArgumentException.class, () -> palette.get(0, 0, -1));
        }
    }

    @Test
    public void testResize() {
        Palette palette = Palette.newPalette(16, 5, 2, 1);
        palette.set(0, 0, 0, 1);
        assertEquals(2, palette.bitsPerEntry());
        palette.set(0, 0, 1, 2);
        assertEquals(2, palette.bitsPerEntry());
        palette.set(0, 0, 2, 3);
        assertEquals(2, palette.bitsPerEntry());

        palette.set(0, 0, 3, 4);
        assertEquals(3, palette.bitsPerEntry());
        assertEquals(1, palette.get(0, 0, 0));
        assertEquals(2, palette.get(0, 0, 1));
        assertEquals(3, palette.get(0, 0, 2));
        assertEquals(4, palette.get(0, 0, 3));
    }
}
