package net.minestom.server.instance;

import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

final class BlockLight {

    static @NotNull Result compute(Palette blockPalette) {
        return null;
    }

    final class Result {
        final byte[] light;
        final byte[] border;

        Result(byte[] light, byte[] border) {
            assert light.length == 16 * 16 * 16 : "Only 16x16x16 sections are supported";
            assert border.length == 16 * 16 * Side.values().length : "There should be 16x16 entries per side";
            this.light = light;
            this.border = border;
        }

        public byte[] light() {
            return light;
        }

        public Border border(Side side) {
            final int length = border.length / Side.values().length;
            final int index = side.ordinal() * length;
            return new Border(Arrays.copyOfRange(border, index, index + length));
        }
    }

    record Border(byte[] light) {
        byte get(int x, int z) {
            return light[x + z * 16];
        }
    }

    public enum Side {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }
}
