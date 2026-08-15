package net.minestom.server.instance.palette;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PaletteAssertions {
    private PaletteAssertions() {
    }

    static List<Palette> testPalettes() {
        return List.of(
                Palette.sized(2, 1, 5, 15, 3),
                Palette.sized(4, 1, 5, 15, 3),
                Palette.sized(8, 1, 5, 15, 3),
                Palette.sized(16, 1, 5, 15, 3),
                Palette.blocks()
        );
    }

    static void assertAllEquals(int expected, Palette palette) {
        final int dim = palette.dimension();
        for (int y = 0; y < dim; y++) {
            for (int z = 0; z < dim; z++) {
                for (int x = 0; x < dim; x++) {
                    assertEquals(expected, palette.get(x, y, z),
                            "Mismatch at (" + x + "," + y + "," + z + ")");
                }
            }
        }
    }
}
