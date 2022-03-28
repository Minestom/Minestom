package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

final class BlockLightCompute {
    private static final Direction[] DIRECTIONS = Direction.values();
    static final int SECTION_SIZE = 16;
    static final int LIGHT_LENGTH = 16 * 16 * 16 / 2;
    static final int SIDE_LENGTH = 16 * 16 * DIRECTIONS.length / 2;

    static @NotNull Result compute(Palette blockPalette) {
        float[] factor = new float[4096];
        byte[] lightArray = new byte[LIGHT_LENGTH];
        byte[][] borders = new byte[DIRECTIONS.length][];
        Arrays.setAll(borders, i -> new byte[SIDE_LENGTH]);
        IntArrayFIFOQueue lightSources = new IntArrayFIFOQueue();

        blockPalette.getAllPresent((x, y, z, stateId) -> {
            final Block block = Block.fromStateId((short) stateId);
            assert block != null;
            final byte lightEmission = (byte) block.registry().lightEmission();

            final int index = x | (z << 4) | (y << 8);
            if (lightEmission > 0) {
                lightSources.enqueue(index | (lightEmission << 12));
                placeLight(lightArray, index, lightEmission);
            }
            factor[index] = getBlockFactor(block);
        });

        while (!lightSources.isEmpty()) {
            final int index = lightSources.dequeueInt();
            final int x = index & 15;
            final int z = (index >> 4) & 15;
            final int y = (index >> 8) & 15;
            final int lightLevel = (index >> 12) & 15;

            for (Direction dir : DIRECTIONS) {
                final int xO = x + dir.normalX();
                final int yO = y + dir.normalY();
                final int zO = z + dir.normalZ();
                final byte newLightLevel = (byte) (lightLevel - 1);
                // Handler border
                if (xO < 0 || xO >= SECTION_SIZE || yO < 0 || yO >= SECTION_SIZE || zO < 0 || zO >= SECTION_SIZE) {
                    final byte[] border = borders[dir.ordinal()];
                    final int borderIndex = switch (dir) {
                        case WEST, EAST -> y * SECTION_SIZE + z;
                        case DOWN, UP -> x * SECTION_SIZE + z;
                        case NORTH, SOUTH -> x * SECTION_SIZE + y;
                    };
                    border[borderIndex] = newLightLevel;
                    continue;
                }
                // Section
                final int newIndex = xO | (zO << 4) | (yO << 8);
                final float targetFactor = factor[newIndex];
                if (targetFactor != 0)
                    continue; // TODO float factor
                if (getLight(lightArray, newIndex) + 2 <= lightLevel) {
                    placeLight(lightArray, newIndex, newLightLevel);
                    lightSources.enqueue(newIndex | (newLightLevel << 12));
                }
            }
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
            } else return (byte) BlockLightCompute.getLight(light, x | (z << 4) | (y << 8));
        }

        public byte[] light() {
            return light;
        }
    }

    private static void placeLight(byte[] light, int index, int value) {
        final int shift = (index & 1) << 2;
        final int i = index >>> 1;
        light[i] = (byte) ((light[i] & (0xF0 >>> shift)) | (value << shift));
    }

    private static int getLight(byte[] light, int index) {
        final int value = light[index >>> 1];
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }

    private static float getBlockFactor(Block block) {
        var shape = block.registry().collisionShape();
        return (float) (shape.relativeStart().isZero() && shape.relativeEnd().samePoint(Vec.ONE) ? 1 : 0);
    }
}
