package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

final class BlockLightCompute {
    private static final BlockFace[] FACES = BlockFace.values();
    private static final Direction[] DIRECTIONS = Direction.values();
    static final int SECTION_SIZE = 16;
    static final int LIGHT_LENGTH = 16 * 16 * 16 / 2;
    static final int SIDE_LENGTH = 16 * 16 * DIRECTIONS.length / 2;

    static @NotNull Result compute(Palette blockPalette, Map<BlockFace, Section> neighbors) {
        for (Map.Entry<BlockFace, Section> entry : neighbors.entrySet()) {
            final BlockFace face = entry.getKey();
            final Section section = entry.getValue();

            Light light = section.blockLight();

            if (light instanceof BlockLight blockLight) {
                byte[] border = blockLight.getBorders()[face.getOppositeFace().ordinal()];
            }
        }

        Block[] blocks = new Block[4096];
        byte[] lightArray = new byte[LIGHT_LENGTH];
        byte[][] borders = new byte[DIRECTIONS.length][];
        Arrays.setAll(borders, i -> new byte[SIDE_LENGTH]);
        IntArrayFIFOQueue lightSources = new IntArrayFIFOQueue();

        blockPalette.getAllPresent((x, y, z, stateId) -> {
            final Block block = Block.fromStateId((short) stateId);
            assert block != null;
            final byte lightEmission = (byte) block.registry().lightEmission();

            final int index = x | (z << 4) | (y << 8);
            blocks[index] = block;
            if (lightEmission > 0) {
                lightSources.enqueue(index | (lightEmission << 12));
                placeLight(lightArray, index, lightEmission);
            }
        });

        while (!lightSources.isEmpty()) {
            final int index = lightSources.dequeueInt();
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
                final Block currentBlock = Objects.requireNonNullElse(blocks[x | (z << 4) | (y << 8)], Block.AIR);
                final Block propagatedBlock = Objects.requireNonNullElse(blocks[newIndex], Block.AIR);
                if (currentBlock.registry().collisionShape().isOccluded(propagatedBlock.registry().collisionShape(), face))
                    continue;
                if (getLight(lightArray, newIndex) + 2 <= lightLevel) {
                    placeLight(lightArray, newIndex, newLightLevel);
                    lightSources.enqueue(newIndex | (newLightLevel << 12));
                }
            }
        }
        return new Result(lightArray, borders);
    }

    record Result(byte[] light, byte[][] borders) {
        Result {
            assert light.length == LIGHT_LENGTH : "Only 16x16x16 sections are supported: " + light.length;
            assert borders.length == FACES.length;
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
                    if (x < 0) return borders[BlockFace.WEST.ordinal()][y * SECTION_SIZE + z];
                    else return borders[BlockFace.EAST.ordinal()][y * SECTION_SIZE + z];
                } else if (outY) {
                    // BOTTOM or TOP
                    if (y < 0) return borders[BlockFace.BOTTOM.ordinal()][x * SECTION_SIZE + z];
                    else return borders[BlockFace.TOP.ordinal()][x * SECTION_SIZE + z];
                } else {
                    // NORTH or SOUTH
                    if (z < 0) return borders[BlockFace.NORTH.ordinal()][x * SECTION_SIZE + y];
                    else return borders[BlockFace.SOUTH.ordinal()][x * SECTION_SIZE + y];
                }
            } else return (byte) BlockLightCompute.getLight(light, x | (z << 4) | (y << 8));
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
}
