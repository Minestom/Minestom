package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Objects;

import static net.minestom.server.instance.light.BlockLight.buildInternalQueue;

final class LightCompute {
    static final BlockFace[] FACES = BlockFace.values();
    static final int LIGHT_LENGTH = 16 * 16 * 16 / 2;
    static final int SIDE_LENGTH = 16 * 16;
    static final int SECTION_SIZE = 16;

    private static final byte[][] emptyBorders = new byte[FACES.length][SIDE_LENGTH];
    static final byte[] emptyContent = new byte[LIGHT_LENGTH];

    static @NotNull Result compute(Palette blockPalette) {
        Block[] blocks = new Block[4096];
        return LightCompute.compute(blocks, buildInternalQueue(blockPalette, blocks));
    }

    static @NotNull Result compute(Block[] blocks, IntArrayFIFOQueue lightPre) {
        if (lightPre.isEmpty()) {
            return new Result(emptyContent, emptyBorders);
        }

        byte[][] borders = new byte[FACES.length][SIDE_LENGTH];
        byte[] lightArray = new byte[LIGHT_LENGTH];

        var lightSources = new LinkedList<Integer>();

        while (!lightPre.isEmpty()) {
            int index = lightPre.dequeueInt();

            final int x = index & 15;
            final int z = (index >> 4) & 15;
            final int y = (index >> 8) & 15;
            final int newLightLevel = (index >> 12) & 15;
            final int newIndex = x | (z << 4) | (y << 8);

            final int oldLightLevel = getLight(lightArray, newIndex);

            if (oldLightLevel < newLightLevel) {
                placeLight(lightArray, newIndex, newLightLevel);
                lightSources.add(index);
            }
        }

        while (!lightSources.isEmpty()) {
            final int index = lightSources.poll();
            final int x = index & 15;
            final int z = (index >> 4) & 15;
            final int y = (index >> 8) & 15;
            final int lightLevel = (index >> 12) & 15;

            for (BlockFace face : FACES) {
                Direction dir = face.toDirection();
                final int xO = x + dir.normalX();
                final int yO = y + dir.normalY();
                final int zO = z + dir.normalZ();
                final byte newLightLevel = (byte) (lightLevel - 1);
                // Handler border
                if (xO < 0 || xO >= SECTION_SIZE || yO < 0 || yO >= SECTION_SIZE || zO < 0 || zO >= SECTION_SIZE) {
                    final byte[] border = borders[face.ordinal()];
                    final int borderIndex = switch (face) {
                        case WEST, EAST -> y * SECTION_SIZE + z;
                        case BOTTOM, TOP -> x * SECTION_SIZE + z;
                        case NORTH, SOUTH -> x * SECTION_SIZE + y;
                    };
                    border[borderIndex] = newLightLevel;
                    continue;
                }
                // Section
                final int newIndex = xO | (zO << 4) | (yO << 8);
                if (getLight(lightArray, newIndex) + 2 <= lightLevel) {
                    final Block currentBlock = Objects.requireNonNullElse(blocks[x | (z << 4) | (y << 8)], Block.AIR);

                    final Block propagatedBlock = Objects.requireNonNullElse(blocks[newIndex], Block.AIR);
                    boolean airAir = currentBlock.isAir() && propagatedBlock.isAir();
                    if (!airAir && currentBlock.registry().collisionShape().isOccluded(propagatedBlock.registry().collisionShape(), face)) continue;
                    placeLight(lightArray, newIndex, newLightLevel);
                    lightSources.add(newIndex | (newLightLevel << 12));
                }
            }
        }
        return new Result(lightArray, borders);
    }

    record Result(byte[] light, byte[][] borders) {
        Result {
            assert light.length == LIGHT_LENGTH : "Only 16x16x16 sections are supported: " + light.length;
        }

        public byte getLight(int x, int y, int z) {
            return (byte) LightCompute.getLight(light, x | (z << 4) | (y << 8));
        }
    }

    private static void placeLight(byte[] light, int index, int value) {
        final int shift = (index & 1) << 2;
        final int i = index >>> 1;
        light[i] = (byte) ((light[i] & (0xF0 >>> shift)) | (value << shift));
    }

    static int getLight(byte[] light, int index) {
        if (index >>> 1 >= light.length) return 0;
        final int value = light[index >>> 1];
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }
}