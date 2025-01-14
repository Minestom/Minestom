package net.minestom.server.instance.palette;

import org.junit.jupiter.api.Test;

public class PaletteIndirectTest {

    @Test
    public void constructor() {
        var palette = new PaletteIndirect(16, 8, (byte) 4);
        palette.set(0, 0, 1, 1);
        var otherPalette = new PaletteIndirect(palette.dimension(), palette.maxBitsPerEntry(), (byte) palette.bitsPerEntry(), palette.count(), palette.paletteToValueList.toIntArray(), palette.values);

        palette.getAll((x, y, z, value) -> {
            assert value == otherPalette.get(x, y, z);
        });
    }
}
