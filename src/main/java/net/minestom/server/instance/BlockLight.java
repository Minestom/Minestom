package net.minestom.server.instance;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

final class BlockLight {
    static final int SECTION_SIZE = 16;
    static final int LIGHT_LENGTH = 16 * 16 * 16 / 2;
    static final int SIDE_LENGTH = 16 * 16 * Direction.values().length / 2;

    static @NotNull Result compute(Palette blockPalette) {

        byte[] lightArray = new byte[LIGHT_LENGTH];
        byte[][] borders = new byte[Direction.values().length][];
        Arrays.setAll(borders, i -> new byte[SIDE_LENGTH]);

        blockPalette.getAllPresent((x, y, z, stateId) -> {
            final Block block = Block.fromStateId((short) stateId);
            assert block != null;
            final byte lightEmission = (byte) block.registry().lightEmission();
            placeLight(lightArray, x, y, z, lightEmission);
        });

        while (true) {
            boolean updated = false;
            for (int x = 0; x < SECTION_SIZE; x++) {
                for (int z = 0; z < SECTION_SIZE; z++) {
                    for (int y = 0; y < SECTION_SIZE; y++) {
                        final byte light = (byte) getLight(lightArray, x, y, z);
                        byte newLight = light;
                        for (Direction dir : Direction.values()) {
                            int xO = x + dir.normalX();
                            int yO = y + dir.normalY();
                            int zO = z + dir.normalZ();

                            if (xO < 0 || xO >= SECTION_SIZE || yO < 0 || yO >= SECTION_SIZE || zO < 0 || zO >= SECTION_SIZE) {
                                final byte[] border = borders[dir.ordinal()];
                                final int index = switch (dir) {
                                    case WEST, EAST -> y * SECTION_SIZE + z;
                                    case DOWN, UP -> x * SECTION_SIZE + z;
                                    case NORTH, SOUTH -> x * SECTION_SIZE + y;
                                };
                                border[index] = (byte) Math.max(border[index], light - 1);
                                continue;
                            }

                            byte neighborLight = (byte) (getLight(lightArray, xO, yO, zO) - 1);
                            neighborLight = (byte) ((float) neighborLight * (1 - getBlockFactor(blockPalette, x, y, z)));
                            newLight = (byte) Math.max(newLight, neighborLight);
                        }
                        if (newLight != light) {
                            updated = true;
                            placeLight(lightArray, x, y, z, newLight);
                        }
                    }
                }
            }
            if (!updated) break;
        }


        return new Result(lightArray, borders);
    }

    static final class Result {
        final byte[] light;
        final byte[][] borders;

        Result(byte[] light, byte[][] borders) {
            assert light.length == LIGHT_LENGTH : "Only 16x16x16 sections are supported: " + light.length;
            //assert border.length == SIDE_LENGTH : "There should be 16x16 entries per side: " + border.length;
            this.light = light;
            this.borders = borders;
        }

        public byte getLight(int x, int y, int z) {
            final boolean outX = x < 0 || x >= SECTION_SIZE;
            final boolean outY = y < 0 || y >= SECTION_SIZE;
            final boolean outZ = z < 0 || z >= SECTION_SIZE;
            if (outX || outY || outZ) {
                final boolean multipleOut = outX ? (outY || outZ) : (outY && outZ);
                if (multipleOut)
                    throw new IllegalArgumentException("Coordinates are out of bounds: " + x + ", " + y + ", " + z);
                if (outX) {
                    // WEST or EAST
                    if (x < 0) return borders[Direction.WEST.ordinal()][y * SECTION_SIZE + z];
                    else return borders[Direction.EAST.ordinal()][y * SECTION_SIZE + z];
                } else if (outY) {
                    // BOTTOM or TOP
                    if (y < 0) return borders[Direction.DOWN.ordinal()][x * SECTION_SIZE + z];
                    else return borders[Direction.UP.ordinal()][x * SECTION_SIZE + z];
                } else {
                    // NORTH or SOUTH
                    if (z < 0) return borders[Direction.NORTH.ordinal()][x * SECTION_SIZE + y];
                    else return borders[Direction.SOUTH.ordinal()][x * SECTION_SIZE + y];
                }
            } else return (byte) BlockLight.getLight(light, x, y, z);
        }

        public byte[] light() {
            return light;
        }
    }

    private static void placeLight(byte[] light, int x, int y, int z, int value) {
        int index = (x & 15) | ((z & 15) << 4) | ((y & 15) << 8);
        int shift = (index & 1) << 2;
        int i = index >>> 1;
        light[i] = (byte) ((light[i] & (0xF0 >>> shift)) | (value << shift));
    }

    private static int getLight(byte[] light, int x, int y, int z) {
        int index = (x & 15) | ((z & 15) << 4) | ((y & 15) << 8);
        int value = light[index >>> 1];
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }

    private static float getBlockFactor(Palette palette, int x, int y, int z) {
        var block = Block.fromStateId((short) palette.get(x, y, z));
        assert block != null : "Block not found: " + x + ", " + y + ", " + z;
        var shape = block.registry().collisionShape();
        float factor = shape.relativeStart().isZero() && shape.relativeEnd().samePoint(Vec.ONE) ? 1 : 0;
        return factor;
    }
}
