package net.minestom.server.instance.palette;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaletteCompareTest {

    @Test
    public void singleValueAgainstEqualIndirect() {
        Palette indirect = Palette.biomes(1);
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++)
                for (int z = 0; z < 4; z++)
                    indirect.set(x, y, z, 1);
        Palette single = Palette.biomes();
        single.fill(1);

        assertTrue(single.compare(indirect));
        assertTrue(indirect.compare(single));
    }

    @Test
    public void singleValueAgainstDifferentIndirect() {
        Palette indirect = Palette.biomes(1);
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++)
                for (int z = 0; z < 4; z++)
                    indirect.set(x, y, z, 2);
        Palette single = Palette.biomes();
        single.fill(1);

        assertFalse(single.compare(indirect));
        assertFalse(indirect.compare(single));
    }

    @Test
    public void partiallyFilledSingleValue() {
        Palette partial = Palette.biomes(1);
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++)
                for (int z = 0; z < 2; z++)
                    partial.set(x, y, z, 1);
        Palette single = Palette.biomes();
        single.fill(1);

        assertFalse(single.compare(partial));
        assertFalse(partial.compare(single));
    }

    @Test
    public void singleValueZeroAgainstEmptyIndirect() {
        Palette empty = Palette.biomes(1);
        Palette single = Palette.biomes();
        single.fill(0);

        assertTrue(single.compare(empty));
        assertTrue(empty.compare(single));
    }

    @Test
    public void twoSingleValues() {
        Palette stone = Palette.biomes();
        stone.fill(1);
        Palette dirt = Palette.biomes();
        dirt.fill(2);
        Palette alsoStone = Palette.biomes();
        alsoStone.fill(1);

        assertTrue(stone.compare(alsoStone));
        assertFalse(stone.compare(dirt));
    }

    @Test
    public void singleValueAgainstDirect() {
        // Value above maxBitsPerEntry forces direct (non-paletted) storage
        Palette direct = Palette.blocks(15);
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
                for (int z = 0; z < 16; z++)
                    direct.set(x, y, z, 20000);
        Palette single = Palette.blocks();
        single.fill(20000);

        assertTrue(single.compare(direct));
        assertTrue(direct.compare(single));
    }
}
